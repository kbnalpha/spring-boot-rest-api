package com.ehspro.service;

import com.ehspro.dto.*;
import com.ehspro.entity.BaseEntity;
import com.ehspro.exception.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.function.Function;

/** Criteria queries keep filtering and pagination in the database, including arbitrary offsets. */
@Service
public class ListQueryService {
    private final EntityManager em;
    private final com.ehspro.security.AccessService access;
    public ListQueryService(EntityManager em, com.ehspro.security.AccessService access) { this.em = em; this.access=access; }

    public <E extends BaseEntity, D> PageResult<D> list(Class<E> type, ListRequest request,
            String businessField, String searchField, Function<E, D> mapper) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<E> query = cb.createQuery(type);
        Root<E> root = query.from(type);
        query.where(predicates(type, request, businessField, searchField, cb, root));
        List<Order> orders = new ArrayList<>();
        Set<String> sortedFields = new HashSet<>();
        if (request.multiSortMeta != null && !request.multiSortMeta.isEmpty()) {
            for (ListRequest.SortField sort : request.multiSortMeta) {
                requireField(type, sort.field);
                orders.add(sort.order < 0 ? cb.desc(root.get(sort.field)) : cb.asc(root.get(sort.field)));
                sortedFields.add(sort.field);
            }
        } else {
            String field = request.sorting == null || request.sorting.isBlank() ? "id" : request.sorting;
            requireField(type, field);
            orders.add("desc".equalsIgnoreCase(request.sortingType) ? cb.desc(root.get(field)) : cb.asc(root.get(field)));
            sortedFields.add(field);
        }
        if (!sortedFields.contains("id")) orders.add(cb.asc(root.get("id")));
        query.orderBy(orders);
        CriteriaQuery<Long> count = cb.createQuery(Long.class);
        Root<E> countRoot = count.from(type);
        count.select(cb.count(countRoot)).where(predicates(type, request, businessField, searchField, cb, countRoot));
        long total = em.createQuery(count).getSingleResult();
        // The specification only defines JSON results, even for the export flag.
        if (request.isExportToExcel && total > 10000) throw ApiException.badRequest("Export exceeds 10000 records; narrow the filters");
        List<E> items = em.createQuery(query).setFirstResult(request.isExportToExcel ? 0 : request.skipCount)
            .setMaxResults(request.isExportToExcel ? 10000 : request.maxResultCount).getResultList();
        return new PageResult<>(items.stream().map(mapper).toList(), total);
    }

    private <E> Predicate[] predicates(Class<E> type, ListRequest r, String businessField, String searchField,
            CriteriaBuilder cb, Root<E> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (businessField != null && !access.isSuperAdmin()) {
            Set<Long> allowed = access.organizationIds();
            predicates.add(allowed.isEmpty() ? cb.disjunction() : root.get(businessField).in(allowed));
        }
        if (type == com.ehspro.entity.Employee.class) {
            if (r.workerType != null) predicates.add(cb.equal(root.get("userType"), r.workerType));
            if (r.systemUsersOnly) {
                var accountQuery = cb.createQuery().subquery(Long.class);
                var account = accountQuery.from(com.ehspro.entity.UserAccount.class);
                accountQuery.select(account.get("employeeId")).where(cb.isTrue(account.get("enabled")));
                predicates.add(root.get("id").in(accountQuery));
                predicates.add(cb.isTrue(root.get("hasAccess")));
                predicates.add(cb.equal(root.get("status"), 1));
            }
        }
        if (r.id != null && r.id > 0) predicates.add(cb.equal(root.get("id"), r.id));
        if (businessField != null && r.businessUnitIds != null && !r.businessUnitIds.isBlank()) {
            try {
                List<Long> ids = Arrays.stream(r.businessUnitIds.split(",", -1)).map(String::trim).map(Long::valueOf).toList();
                if (ids.stream().anyMatch(id -> id <= 0)) throw new NumberFormatException();
                if (!access.isSuperAdmin() && !access.organizationIds().containsAll(ids)) throw new org.springframework.security.access.AccessDeniedException("Requested organizations are outside your scope");
                if (type == com.ehspro.entity.Employee.class) {
                    predicates.add(cb.or(ids.stream().map(id -> cb.isMember(id, root.<Collection<Long>>get("organizationUnitIds"))).toArray(Predicate[]::new)));
                } else predicates.add(root.get(businessField).in(ids));
            } catch (NumberFormatException e) { throw ApiException.badRequest("businessUnitIds must contain comma-separated positive IDs"); }
        }
        if (r.filter != null && !r.filter.isBlank()) predicates.add(cb.like(cb.lower(root.get(searchField)), "%" + escape(r.filter.toLowerCase(Locale.ROOT)) + "%", '\\'));
        if (r.filters != null) for (ListRequest.ColumnFilter filter : r.filters) {
            Class<?> fieldType = requireField(type, filter.field);
            Path<?> path = root.get(filter.field);
            String mode = filter.matchMode == null ? "equals" : filter.matchMode;
            if (filter.value == null || filter.value.isNull()) {
                if (!Set.of("equals", "notEquals").contains(mode)) throw ApiException.badRequest("Null filters require equals or notEquals");
                predicates.add(mode.equals("equals") ? cb.isNull(path) : cb.isNotNull(path));
            } else if (Set.of("contains", "startsWith", "endsWith").contains(mode)) {
                if (fieldType != String.class) throw ApiException.badRequest("Text match requires a string field: " + filter.field);
                String value = escape(filter.value.asText().toLowerCase(Locale.ROOT));
                String pattern = (mode.equals("startsWith") ? "" : "%") + value + (mode.equals("endsWith") ? "" : "%");
                predicates.add(cb.like(cb.lower(path.as(String.class)), pattern, '\\'));
            } else if (Set.of("equals", "notEquals").contains(mode)) {
                Object value = convert(filter.value.asText(), fieldType);
                predicates.add(mode.equals("equals") ? cb.equal(path, value) : cb.notEqual(path, value));
            } else throw ApiException.badRequest("Unsupported filter matchMode: " + mode);
        }
        return predicates.toArray(Predicate[]::new);
    }
    private Class<?> requireField(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            if (!Set.of(String.class, Long.class, Integer.class, Boolean.class, LocalDate.class, LocalDateTime.class).contains(field.getType())) throw new NoSuchFieldException();
            return field.getType();
        } catch (NoSuchFieldException | NullPointerException e) { throw ApiException.badRequest("Unsupported sort/filter field: " + name); }
    }
    private Object convert(String value, Class<?> type) {
        try {
            if (type == Long.class) return Long.valueOf(value);
            if (type == Integer.class) return Integer.valueOf(value);
            if (type == LocalDate.class) return LocalDate.parse(value);
            if (type == LocalDateTime.class) return LocalDateTime.parse(value);
            if (type == Boolean.class) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) throw new IllegalArgumentException();
                return Boolean.valueOf(value);
            }
            return value;
        } catch (RuntimeException e) { throw ApiException.badRequest("Invalid filter value for " + type.getSimpleName()); }
    }
    private String escape(String value) { return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_"); }
}

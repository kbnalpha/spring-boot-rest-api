package com.ehspro.entity;
import java.io.Serializable;
import java.util.Objects;
public class ReferenceItemId implements Serializable {
    public String kind;
    public Long id;
    public ReferenceItemId() {}
    public ReferenceItemId(String kind,Long id) { this.kind=kind; this.id=id; }
    @Override public boolean equals(Object other) { return other instanceof ReferenceItemId r && Objects.equals(kind,r.kind) && Objects.equals(id,r.id); }
    @Override public int hashCode() { return Objects.hash(kind,id); }
}

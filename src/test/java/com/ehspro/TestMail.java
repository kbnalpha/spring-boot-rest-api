package com.ehspro;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.Mockito;
final class TestMail {
    static String temporaryPassword(JavaMailSender mail,String email) {
        String password=null;
        for(var invocation:Mockito.mockingDetails(mail).getInvocations()) {
            if(invocation.getArguments().length==1&&invocation.getArgument(0) instanceof SimpleMailMessage message
                    &&java.util.Arrays.asList(message.getTo()).contains(email)) {
                password=message.getText().lines().filter(l -> l.startsWith("Temporary password: ")).findFirst().orElseThrow().substring(20);
            }
        }
        if(password==null) throw new AssertionError("No activation email captured for "+email);
        return password;
    }
}

package org.chous.taco.services;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MailService {

    public void send(String to, String subject, String text) {

        to = "kidminsk@yandex.ru";
        String from = "kidimpminsk@gmail.com";
        final String username = "kidimpminsk";
        final String password = "jgpdnhecvehsuwdm";

        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");



        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });



        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            message.setSubject(subject);

            message.setText(text);

//            setAttachment(message, "/Users/pras/Desktop/text.txt");

            Transport.send(message);

            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }



//    public static void setAttachment(Message message, String filename) throws MessagingException {
//        message.setText(filename);
//
//        // Создание и заполнение первой части
//        MimeBodyPart p1 = new MimeBodyPart();
//        p1.setText("This is part one of a test multipart e-mail." +
//                "The second part is file as an attachment");
//
//        // Добавление файла во вторую часть
//        FileDataSource fds = new FileDataSource(filename);
//        p1.setDataHandler(new DataHandler(fds));
//        p1.setFileName(fds.getName());
//
//        // Создание экземпляра класса Multipart. Добавление частей сообщения в него.
//        Multipart mp = new MimeMultipart();
//        mp.addBodyPart(p1);
//
//        // Установка экземпляра класса Multipart в качестве контента документа
//        message.setContent(mp);
//    }

}
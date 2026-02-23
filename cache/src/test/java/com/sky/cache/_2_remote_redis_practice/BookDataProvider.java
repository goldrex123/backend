package com.sky.cache._2_remote_redis_practice;

import com.sky.cache._1_practice.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookDataProvider {

    public static List<Book> createRandomBooks(int count) {
        List<Book> books = new ArrayList<>(count);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            String randomName = generateRandomString(random.nextInt(12) + 1);
            boolean randomIsSoldOut = random.nextBoolean();
            books.add(Book.builder()
                    .name(randomName)
                    .soldOut(randomIsSoldOut)
                    .build());
        }
        return books;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}

package com.stchat.server.web.server;

import com.stchat.server.web.controller.ConversationController;
import com.stchat.server.web.controller.MessageController;
import io.javalin.Javalin;

public class ConversationServer {
    public static void start() {
        Javalin app = Javalin.create()
                .before(ctx -> {
                    ctx.header("Access-Control-Allow-Origin", "*");
                    ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
                })
                .options("/*", ctx -> {
                    ctx.status(200);
                })
                .start(7071);
        ConversationController.registerRoutes(app);
        MessageController.registerRoutes(app);

        System.out.println("Conversation server is running at http://localhost:7071/api/conversations");
    }
}

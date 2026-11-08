package com.fiap.feedbackhub.infra;

import software.amazon.awscdk.App;

public class FeedbackhubApp {

    public static void main(final String[] args) {
        App app = new App();

        new FeedbackhubStack(app, "FeedbackhubStack");

        app.synth();
    }
}

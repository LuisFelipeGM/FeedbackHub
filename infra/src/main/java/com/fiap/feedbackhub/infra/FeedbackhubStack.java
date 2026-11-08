package com.fiap.feedbackhub.infra;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class FeedbackhubStack extends Stack {

    public FeedbackhubStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public FeedbackhubStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
    }
}

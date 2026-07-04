package com.quizzybee.util;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public final class BeeCursorEffect {
    private static final double BEE_WIDTH = 48;
    private static final double CURSOR_OFFSET_X = -24;
    private static final double CURSOR_OFFSET_Y = 12;
    private static final double TRAIL_FACTOR = 0.18;
    private static final double FLOAT_AMPLITUDE = 6;

    private BeeCursorEffect() {
    }

    public static Parent wrapWithBeeOverlay(Parent contentRoot) {
        StackPane wrapper = new StackPane();
        wrapper.getStyleClass().addAll(contentRoot.getStyleClass());
        contentRoot.getStyleClass().clear();
        wrapper.getChildren().add(contentRoot);
        return wrapper;
    }

    public static void attach(Scene scene) {
        if (!(scene.getRoot() instanceof StackPane wrapper)) {
            return;
        }

        ImageView beeView = new ImageView(new Image(BeeCursorEffect.class.getResource("/view/assets/cursor-bee.png").toExternalForm()));
        beeView.setFitWidth(BEE_WIDTH);
        beeView.setPreserveRatio(true);
        beeView.setManaged(false);
        beeView.setMouseTransparent(true);
        beeView.setPickOnBounds(false);
        beeView.setVisible(false);

        wrapper.getChildren().add(beeView);

        final double[] targetX = {120};
        final double[] targetY = {120};
        final double[] currentX = {120};
        final double[] currentY = {120};
        final double[] previousX = {120};

        scene.setOnMouseMoved(event -> {
            targetX[0] = event.getSceneX() + CURSOR_OFFSET_X;
            targetY[0] = event.getSceneY() + CURSOR_OFFSET_Y;
            if (!beeView.isVisible()) {
                currentX[0] = targetX[0];
                currentY[0] = targetY[0];
                previousX[0] = targetX[0];
                beeView.setVisible(true);
            }
        });

        scene.setOnMouseDragged(event -> {
            targetX[0] = event.getSceneX() + CURSOR_OFFSET_X;
            targetY[0] = event.getSceneY() + CURSOR_OFFSET_Y;
        });

        scene.setOnMouseExited(event -> beeView.setVisible(false));
        scene.setOnMouseEntered(event -> {
            targetX[0] = event.getSceneX() + CURSOR_OFFSET_X;
            targetY[0] = event.getSceneY() + CURSOR_OFFSET_Y;
            beeView.setVisible(true);
        });

        AnimationTimer timer = new AnimationTimer() {
            private long startNanos = -1;

            @Override
            public void handle(long now) {
                if (!beeView.isVisible()) {
                    return;
                }

                if (startNanos < 0) {
                    startNanos = now;
                }

                currentX[0] += (targetX[0] - currentX[0]) * TRAIL_FACTOR;
                currentY[0] += (targetY[0] - currentY[0]) * TRAIL_FACTOR;

                double elapsedSeconds = (now - startNanos) / 1_000_000_000.0;
                double floatOffset = Math.sin(elapsedSeconds * 3.4) * FLOAT_AMPLITUDE;
                double deltaX = currentX[0] - previousX[0];
                double rotation = Math.max(-18, Math.min(18, deltaX * 2.4)) + Math.sin(elapsedSeconds * 5.2) * 3;

                beeView.setTranslateX(currentX[0]);
                beeView.setTranslateY(currentY[0] + floatOffset);
                beeView.setRotate(rotation);
                previousX[0] = currentX[0];
            }
        };
        timer.start();

        wrapper.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                timer.stop();
            }
        });
    }
}

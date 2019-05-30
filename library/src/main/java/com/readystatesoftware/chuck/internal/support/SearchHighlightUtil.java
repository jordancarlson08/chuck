package com.readystatesoftware.chuck.internal.support;

import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class SearchHighlightUtil {


    public static List<BackgroundColorSpan> format(TextView view, String text, String criteria) {
        List<BackgroundColorSpan> spans = new ArrayList<>();

        List<Integer> startIndexes = indexesOf(text.toLowerCase(), criteria.toLowerCase());
        view.setText(applySpannable(text, startIndexes, criteria.length(), spans));

        return spans;
    }

    private static List<Integer> indexesOf(String text, String criteria) {
        List<Integer> startPositions = new ArrayList<>();
        int index = text.indexOf(criteria);
        do {
            startPositions.add(index);
            index = text.indexOf(criteria, index + 1);
        } while (index >= 0);
        return startPositions;
    }

    private static SpannableStringBuilder applySpannable(String text, List<Integer> indexes, int length, List<BackgroundColorSpan> spans) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (Integer position : indexes) {
//            builder.setSpan(new UnderlineSpan(), position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            builder.setSpan(new ForegroundColorSpan(Color.RED), position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            BackgroundColorSpan bgSpan = new BackgroundColorSpan(Color.YELLOW);
            spans.add(bgSpan);
            builder.setSpan(bgSpan, position, position + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public static Rect matchLocation(WindowManager manager, TextView parentTextView, BackgroundColorSpan span) {
        Rect parentTextViewRect = new Rect();

// Initialize values for the computing of clickedText position
        SpannableString completeText = (SpannableString) (parentTextView).getText();
        Layout textViewLayout = parentTextView.getLayout();

        double startOffsetOfClickedText = completeText.getSpanStart(span);
        double endOffsetOfClickedText = completeText.getSpanEnd(span);
        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) startOffsetOfClickedText);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) endOffsetOfClickedText);


// Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset((int) startOffsetOfClickedText);
        int currentLineEndOffset = textViewLayout.getLineForOffset((int) endOffsetOfClickedText);
        boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);


// Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = {0, 0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);

        double parentTextViewTopAndBottomOffset = (
                parentTextViewLocation[1] -
                        parentTextView.getScrollY() +
                        parentTextView.getCompoundPaddingTop()
        );
        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

// In the case of multi line text, we have to choose what rectangle take
        if (keywordIsInMultiLine) {

            int screenHeight = manager.getDefaultDisplay().getHeight();
            int dyTop = parentTextViewRect.top;
            int dyBottom = screenHeight - parentTextViewRect.bottom;
            boolean onTop = dyTop > dyBottom;

            if (onTop) {
                endXCoordinatesOfClickedText = textViewLayout.getLineRight(currentLineStartOffset);
            } else {
                parentTextViewRect = new Rect();
                textViewLayout.getLineBounds(currentLineEndOffset, parentTextViewRect);
                parentTextViewRect.top += parentTextViewTopAndBottomOffset;
                parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;
                startXCoordinatesOfClickedText = textViewLayout.getLineLeft(currentLineEndOffset);
            }

        }

        parentTextViewRect.left += (
                parentTextViewLocation[0] +
                        startXCoordinatesOfClickedText +
                        parentTextView.getCompoundPaddingLeft() -
                        parentTextView.getScrollX()
        );
        parentTextViewRect.right = (int) (
                parentTextViewRect.left +
                        endXCoordinatesOfClickedText -
                        startXCoordinatesOfClickedText
        );

        return parentTextViewRect;
    }

}
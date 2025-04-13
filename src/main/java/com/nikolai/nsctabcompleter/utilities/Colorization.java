package com.nikolai.nsctabcompleter.utilities;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;

public class Colorization
{
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6}(?:\\s*,\\s*#[A-Fa-f0-9]{6})+)>((?:(?!</#Gradient>).)*?)</#Gradient>");

    public static String applyColorization(String string)
    {
        string = applyGradient(string);
        string = applyHex(string);
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }
    private static String applyHex(String input)
    {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find())
        {
            String hexCode = matcher.group(1);
            try
            {
                matcher.appendReplacement(buffer, ChatColor.of("#" + hexCode).toString());
            }
            catch (IllegalArgumentException e)
            {
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    private static String applyGradient(String input)
    {
        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find())
        {
            String[] hexColors = matcher.group(1).split(",");
            List<String> colors = Arrays.stream(hexColors)
                    .map(String::trim)
                    .collect(Collectors.toList());

            String text = matcher.group(2);
            String colored = applyMultipleGradient(text, colors);
            matcher.appendReplacement(result, Matcher.quoteReplacement(colored));
        }
        matcher.appendTail(result);
        return result.toString();
    }
    private static String applyMultipleGradient(String text, List<String> colors)
    {
        if (colors.size() < 2 || text.length() == 0)
        {
            return text;
        }

        int segments = colors.size() - 1;
        int segmentLength = text.length() / segments;
        int remainder = text.length() % segments;

        StringBuilder result = new StringBuilder();
        int charIndex = 0;

        for (int i = 0; i < segments; i++)
        {
            int segLen = segmentLength + (i < remainder ? 1 : 0);
            int[] rgbStart = HexToRgb(colors.get(i));
            int[] rgbEnd = HexToRgb(colors.get(i + 1));

            for (int j = 0; j < segLen; j++)
            {
                double ratio = segLen == 1 ? 0 : (double) j / (segLen - 1);
                int r = (int) (rgbStart[0] + (rgbEnd[0] - rgbStart[0]) * ratio);
                int g = (int) (rgbStart[1] + (rgbEnd[1] - rgbStart[1]) * ratio);
                int b = (int) (rgbStart[2] + (rgbEnd[2] - rgbStart[2]) * ratio);

                ChatColor color = ChatColor.of(String.format("#%02x%02x%02x", r, g, b));
                result.append(color).append(text.charAt(charIndex++));
            }
        }
        return result.toString();
    }
    private static int[] HexToRgb(String hex)
    {
        hex = hex.replace("#", "");
        return new int[]
        {
                Integer.valueOf(hex.substring(0, 2), 16),
                Integer.valueOf(hex.substring(2, 4), 16),
                Integer.valueOf(hex.substring(4, 6), 16)
        };
    }
}

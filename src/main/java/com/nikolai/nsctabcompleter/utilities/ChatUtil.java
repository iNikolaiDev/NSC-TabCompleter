package com.nikolai.nsctabcompleter.utilities;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatUtil — lightweight chat utility for NSC TabCompleter.
 *
 * colour(String)     → applies hex, gradient, and & codes
 * centerText(String) → pixel-accurate chat centering (320px wide)
 */
public final class ChatUtil
{
    private ChatUtil() {}

    // ── Patterns ──────────────────────────────────────────────────
    private static final Pattern HEX      = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT = Pattern.compile(
        "<(#[A-Fa-f0-9]{6}(?:\\s*,\\s*#[A-Fa-f0-9]{6})+)>(.*?)</#Gradient>",
        Pattern.DOTALL
    );

    // ── Version check cached once at class-load ───────────────────
    private static final boolean SUPPORTS_HEX = supportsHex();

    private static boolean supportsHex()
    {
        try
        {
            String v = Bukkit.getBukkitVersion().split("-")[0]; // e.g. "1.21.4"
            String[] parts = v.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major > 1 || minor >= 16;
        }
        catch (Exception e)
        {
            return true; // assume modern if parsing fails
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  colour()
    // ═════════════════════════════════════════════════════════════

    /**
     * Applies gradient tags, hex codes, and & colour codes to a string.
     * On 1.13–1.15 servers, hex/gradient is stripped gracefully.
     */
    public static String colour(String text)
    {
        if (text == null || text.isEmpty()) return text;

        if (!SUPPORTS_HEX)
        {
            // Strip hex and gradient tags, keep & codes only
            String stripped = text
                .replaceAll("<#[^>]+>.*?</#Gradient>", "")
                .replaceAll("&#[A-Fa-f0-9]{6}", "");
            return ChatColor.translateAlternateColorCodes('&', stripped);
        }

        text = applyGradient(text);
        text = applyHex(text);
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    // ─── Hex ──────────────────────────────────────────────────────

    private static String applyHex(String text)
    {
        Matcher m = HEX.matcher(text);
        if (!m.find()) return text; // fast-path: no hex in string

        StringBuffer sb = new StringBuffer();
        do
        {
            try
            {
                m.appendReplacement(sb, ChatColor.of("#" + m.group(1)).toString());
            }
            catch (IllegalArgumentException e)
            {
                m.appendReplacement(sb, "");
            }
        }
        while (m.find());

        m.appendTail(sb);
        return sb.toString();
    }

    // ─── Gradient ─────────────────────────────────────────────────

    private static String applyGradient(String text)
    {
        Matcher m = GRADIENT.matcher(text);
        if (!m.find()) return text; // fast-path: no gradient in string

        StringBuffer sb = new StringBuffer();
        do
        {
            String[] stops  = m.group(1).split("\\s*,\\s*");
            String   body   = m.group(2);
            m.appendReplacement(sb, Matcher.quoteReplacement(buildGradient(body, stops)));
        }
        while (m.find());

        m.appendTail(sb);
        return sb.toString();
    }

    private static String buildGradient(String text, String[] stops)
    {
        int len = text.length();
        if (stops.length < 2 || len == 0) return text;

        int          segments = stops.length - 1;
        int          segLen   = len / segments;
        int          rem      = len % segments;
        StringBuilder result  = new StringBuilder(len * 8);
        int          charIdx  = 0;

        for (int s = 0; s < segments; s++)
        {
            int   sLen  = segLen + (s < rem ? 1 : 0);
            int[] start = hexToRgb(stops[s]);
            int[] end   = hexToRgb(stops[s + 1]);

            for (int j = 0; j < sLen; j++)
            {
                double t = (sLen == 1) ? 0.0 : (double) j / (sLen - 1);
                int r = (int) (start[0] + (end[0] - start[0]) * t);
                int g = (int) (start[1] + (end[1] - start[1]) * t);
                int b = (int) (start[2] + (end[2] - start[2]) * t);
                result.append(ChatColor.of(String.format("#%02x%02x%02x", r, g, b)));
                result.append(text.charAt(charIdx++));
            }
        }
        return result.toString();
    }

    private static int[] hexToRgb(String hex)
    {
        hex = hex.trim().replace("#", "");

        return new int[]
        {
            Integer.parseInt(hex, 0, 2, 16),
            Integer.parseInt(hex, 2, 4, 16),
            Integer.parseInt(hex, 4, 6, 16)
        };
    }

    // ═════════════════════════════════════════════════════════════
    //  centerText()
    // ═════════════════════════════════════════════════════════════

    private static final int CHAT_PX    = 300; // default chat width in pixels
    private static final int SPACE_PX   = 4;   // width of a single space
    private static final int BOLD_EXTRA = 1;   // extra px per char when bold

    // Pixel widths for ASCII 32–126
    private static final byte[] CHAR_WIDTHS =
    {
    //  sp  !  "  #  $  %  &  '  (  )  *  +  ,  -  .  /
        4,  2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6,
    //   0  1  2  3  4  5  6  7  8  9  :  ;  <  =  >  ?
        6,  6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6,
    //   @  A  B  C  D  E  F  G  H  I  J  K  L  M  N  O
        7,  6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6, 6, 6,
    //   P  Q  R  S  T  U  V  W  X  Y  Z  [  \  ]  ^  _
        6,  6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6,
    //   `  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o
        3,  6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 4, 6, 6, 6,
    //   p  q  r  s  t  u  v  w  x  y  z  {  |  }  ~
        6,  6, 5, 6, 6, 6, 6, 6, 6, 6, 6, 5, 2, 5, 7
    };

    /**
     * Centers text in Minecraft chat by prepending spaces.
     *
     * Strips §colour codes before measuring so colours don't shift the result.
     * Handles §l (bold) by adding +1px per bold character automatically.
     *
     * @param text  Raw text, may include §colour / gradient codes
     * @return      Text padded with leading spaces to appear centred
     */
    public static String centerText(String text)
    {
        if (text == null || text.isEmpty()) return text;

        int textPx  = measureWidth(text);
        int padding = (CHAT_PX - textPx) / 2;
        if (padding <= 0) return text;

        return " ".repeat(padding / SPACE_PX) + text;
    }

    /** Measures rendered pixel width of a string, respecting §l bold. */
    private static int measureWidth(String text)
    {
        int     width = 0;
        boolean bold  = false;

        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length())
            {
                char code = Character.toLowerCase(text.charAt(++i));
                if (code == 'l') bold  = true;
                if (code == 'r') bold  = false;
                // skip all other colour codes — they have no pixel width
                continue;
            }

            int idx = c - 32;
            int w   = (idx >= 0 && idx < CHAR_WIDTHS.length) ? CHAR_WIDTHS[idx] : 6;
            width  += bold ? w + BOLD_EXTRA : w;
        }

        return width;
    }
}
package local.umiioq1.repaircontrol;

import java.nio.charset.StandardCharsets;

/** Pure, Android-independent validation. No device state is modified here. */
public final class RecoveryRules {
    private RecoveryRules() { }
    public static final String PROJECTION_PACKAGE = "com.telanda.keystone";
    public static final String PROJECTION_MAIN = "com.telanda.keystone.MainActivity";

    public static boolean validSsid(String s) {
        if (s == null || s.isEmpty() || s.getBytes(StandardCharsets.UTF_8).length > 32) return false;
        for (int i=0; i<s.length(); i++) if (Character.isISOControl(s.charAt(i))) return false;
        return true;
    }
    public static boolean hexPsk(String s) { return s != null && s.matches("[0-9a-fA-F]{64}"); }
    public static boolean validPsk(String s) {
        if (hexPsk(s)) return true;
        if (s == null || s.length() < 8 || s.length() > 63) return false;
        for (int i=0; i<s.length(); i++) if (s.charAt(i)<32 || s.charAt(i)>126) return false;
        return true;
    }
    public static String quote(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
    public static String unquote(String s) {
        if (s == null) return "";
        return s.length()>1 && s.startsWith("\"") && s.endsWith("\"") ? s.substring(1,s.length()-1) : s;
    }
    public static int integer(String s) {
        try { return Integer.parseInt(s); } catch (Exception ignored) { return -1; }
    }
    public static boolean rectangle(int w, int h, String lt, String rt, String lb, String rb) {
        return w>0 && h>0 && ("0,"+h).equals(lt) && (w+","+h).equals(rt)
                && "0,0".equals(lb) && (w+",0").equals(rb);
    }
    public static String ipv4(int ip) {
        return (ip & 255)+"."+((ip>>>8)&255)+"."+((ip>>>16)&255)+"."+((ip>>>24)&255);
    }
}

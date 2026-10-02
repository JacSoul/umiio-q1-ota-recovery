import local.umiioq1.repaircontrol.RecoveryRules;

public final class RecoveryRulesTest {
    private static int tests;
    private static void check(boolean condition) { tests++; if (!condition) throw new AssertionError("Test "+tests); }
    public static void main(String[] args) {
        check(RecoveryRules.PROJECTION_MAIN.equals("com.telanda.keystone.MainActivity"));
        check(RecoveryRules.validSsid("My network"));
        check(RecoveryRules.validSsid(" leading space "));
        check(!RecoveryRules.validSsid(""));
        check(!RecoveryRules.validSsid(null));
        check(!RecoveryRules.validSsid("bad\nssid"));
        check(RecoveryRules.validSsid(new String(new char[32]).replace('\0','a')));
        check(!RecoveryRules.validSsid(new String(new char[33]).replace('\0','a')));
        check(RecoveryRules.validSsid(new String(new char[16]).replace('\0','я')));
        check(!RecoveryRules.validSsid(new String(new char[17]).replace('\0','я')));
        check(RecoveryRules.validPsk("abcdefgh"));
        check(!RecoveryRules.validPsk("abcdefg"));
        check(!RecoveryRules.validPsk("abc\ndefgh"));
        check(!RecoveryRules.validPsk("парольпароль"));
        check(RecoveryRules.validPsk(new String(new char[63]).replace('\0','x')));
        check(!RecoveryRules.validPsk(new String(new char[64]).replace('\0','x')));
        check(RecoveryRules.hexPsk(new String(new char[64]).replace('\0','A')));
        check(RecoveryRules.quote("a\"b\\c").equals("\"a\\\"b\\\\c\""));
        check(RecoveryRules.unquote("\"network\"").equals("network"));
        check(RecoveryRules.rectangle(1024,600,"0,600","1024,600","0,0","1024,0"));
        check(!RecoveryRules.rectangle(1024,600,"0,777","917,282","0,0","917,0"));
        check(!RecoveryRules.rectangle(0,0,"0,0","0,0","0,0","0,0"));
        check(!RecoveryRules.rectangle(1024,600,"?","1024,600","0,0","1024,0"));
        check(RecoveryRules.integer("1000")==1000);
        check(RecoveryRules.integer("?")==-1);
        check(RecoveryRules.ipv4(0x0100007f).equals("127.0.0.1"));
        System.out.println("PASS: "+tests+" pure logic checks (not an Android runtime test).");
    }
}

package local.umiioq1.repaircontrol;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/** Projection is read-only. The sole OEM launch target is MainActivity.
 * Wi-Fi changes require explicit confirmation; credentials go only to Android's WifiManager.
 * This app neither repairs system packages nor invokes a reset or vendor interface.
 */
public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView projectionStatus, wifiStatus;
    private EditText ssidInput, passwordInput;
    private Spinner security;
    private Button connectButton;
    private boolean registered, pending, connectedEvent;
    private String pendingSsid, wifiResult = "";
    private static final int WIFI_PERMISSION = 42;

    private String t(String ru, String en) {
        return Locale.getDefault().getLanguage().equals("ru") ? ru : en;
    }

    private final Runnable timeout = new Runnable() {
        @Override public void run() {
            if (pending) fail(t("За 60 секунд подключение с IPv4 не подтверждено. Конфигурация могла сохраниться в Android.",
                    "Connection with IPv4 was not confirmed within 60 seconds. Android may have saved the configuration."));
        }
    };
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!pending) return;
            verifyConnection();
            if (pending) handler.postDelayed(this, 1000);
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent intent) {
            if (!pending || !WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) return;
            NetworkInfo n = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (n != null && n.isConnected()) { connectedEvent = true; verifyConnection(); }
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
    }
    @Override public void onResume() { super.onResume(); refresh(); }
    @Override public void onStop() {
        if (passwordInput != null) passwordInput.setText("");
        super.onStop();
    }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (registered) { try { unregisterReceiver(receiver); } catch (Exception ignored) { } }
        pending = false; pendingSsid = null;
        super.onDestroy();
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24),dp(16),dp(24),dp(24));
        root.setBackgroundColor(Color.rgb(245,247,250));
        scroll.addView(root);
        addText(root,"Umiio Q1 Repair Control 2.0.0",24);
        addText(root,t("Проверенный способ: Q1 / Android 9. Q2 и другие модели не проверены.\nЭто не прошивка и не автоматический reset.",
                "Recovery workflow tested on Q1 / Android 9. Q2 and other models are untested.\nThis is not firmware or an automatic reset."),15);
        addText(root,t("1. Zoom / Keystone — только чтение","1. Zoom / Keystone — read-only"),21);
        projectionStatus = addText(root,"",17);
        projectionStatus.setTextIsSelectable(true);
        addButton(root,t("Обновить статус","Refresh status"),v -> refresh());
        addButton(root,"Open Projection Settings",v -> openProjection());
        addText(root,t("Правильно: MainActivity → штатный пункт Keystone → ручная настройка углов.\nНЕ нажимайте MENU Reset. Не открывайте ручной экран напрямую из других launchers. Если уже открывали — обычная перезагрузка, затем сначала главное меню ProjectionSettings.",
                "Correct: MainActivity → stock Keystone menu → adjust corners manually.\nDo NOT press MENU Reset or open the manual screen directly from other launchers. If you already did, restart normally and open ProjectionSettings main menu first."),16);
        addText(root,t("2. Wi-Fi через WifiManager","2. Wi-Fi through WifiManager"),21);
        addText(root,t("Введите свою сеть. WPA/WPA2 Personal или открытая сеть. WPA3-only / Enterprise не поддерживаются этим помощником. Предпочтите 2.4 GHz, если поддержка 5 GHz неизвестна.",
                "Enter your own network. WPA/WPA2 Personal or open network. This helper does not support WPA3-only / Enterprise. Prefer 2.4 GHz if 5 GHz support is unknown."),15);
        ssidInput = new EditText(this);
        ssidInput.setHint(t("Точное имя сети (SSID)","Exact network name (SSID)"));
        ssidInput.setSingleLine(true);
        ssidInput.setSaveEnabled(false);
        root.addView(ssidInput,params());
        passwordInput = new EditText(this);
        passwordInput.setHint(t("Пароль Wi-Fi","Wi-Fi password"));
        passwordInput.setSingleLine(true);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setSaveEnabled(false);
        if (Build.VERSION.SDK_INT>=26) passwordInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        root.addView(passwordInput,params());
        security = new Spinner(this);
        security.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"WPA/WPA2-PSK",t("Открытая сеть (без шифрования)","Open network (unencrypted)")}));
        root.addView(security,params());
        connectButton=addButton(root,t("Подключить Wi-Fi — показать план","Connect Wi-Fi — preview changes"),v -> previewWifi());
        wifiStatus=addText(root,"",16);
        wifiStatus.setTextIsSelectable(true);
        addText(root,t("Пароль не вшит в APK и не записывается приложением в файлы/логи. Android сохраняет параметры сети для подключения. Интернет-доступ и изоляция клиентов этим статусом не проверяются.",
                "No password is embedded or written to app files/logs. Android saves network credentials for connection. This status does not test Internet access or client isolation."),15);
        addText(root,t("Не требуются: root, ADB, прошивка, FactoryMode, Auto_set_zero. Приложение не записывает Zoom/Keystone, не меняет системные APK и не выполняет reboot. Bluetooth в эту версию не входит.",
                "No root, ADB, flashing, FactoryMode or Auto_set_zero required. This app does not write Zoom/Keystone, replace system APKs or reboot. Bluetooth is not included in this release."),15);
        return scroll;
    }

    private void openProjection() {
        if (pending) { alert(t("Сначала дождитесь Wi-Fi","Wait for Wi-Fi to finish")); return; }
        new AlertDialog.Builder(this).setTitle("Open Projection Settings")
                .setMessage(t("Откроется только главное меню com.telanda.keystone.MainActivity. Приложение не задаёт координаты. Выберите в нём Keystone и меняйте углы самостоятельно. НЕ нажимайте MENU Reset.",
                        "Only com.telanda.keystone.MainActivity will open. This app does not set coordinates. Select Keystone in that menu and adjust manually. Do NOT press MENU Reset."))
                .setNegativeButton(t("Отмена","Cancel"),null)
                .setPositiveButton(t("Открыть главное меню","Open main menu"),(d,w) -> {
                    Intent intent=new Intent();
                    intent.setComponent(new ComponentName(RecoveryRules.PROJECTION_PACKAGE,RecoveryRules.PROJECTION_MAIN));
                    try { startActivity(intent); }
                    catch (Exception e) { alert(t("Штатное главное меню недоступно. Обходной запуск других Activity не выполняется.",
                            "OEM main menu is unavailable. No fallback activity will be launched.")+"\n"+e.getClass().getSimpleName()); }
                }).show();
    }

    private void refresh() { renderProjection(); renderWifi(); }
    private void renderProjection() {
        String raw=property("persist.display.keystone_scale");
        int scale=RecoveryRules.integer(raw);
        int w=RecoveryRules.integer(property("persist.sys.keystone.width"));
        int h=RecoveryRules.integer(property("persist.sys.keystone.height"));
        String lt=property("persist.sys.keystone.lt"),rt=property("persist.sys.keystone.rt");
        String lb=property("persist.sys.keystone.lb"),rb=property("persist.sys.keystone.rb");
        boolean full=RecoveryRules.rectangle(w,h,lt,rt,lb,rb);
        boolean unknown=w<=0 || h<=0 || "?".equals(lt) || "?".equals(rt) || "?".equals(lb) || "?".equals(rb);
        String zoom=scale<0 ? "UNKNOWN" : String.format(Locale.US,"%.1f%%\nscale=%.3f\nraw=%d",scale/10.0,scale/1000.0,scale);
        projectionStatus.setText("Zoom: "+zoom+"\nLT="+lt+"\nRT="+rt+"\nLB="+lb+"\nRB="+rb+
                "\nOEM canvas: "+w+" x "+h+"\nGeometry: "+(unknown ? "UNKNOWN" : full ? "FULL RECTANGLE" : "NOT FULL RECTANGLE")+
                "\nQ1 reference: "+(w==1024 && h==600 && full && scale==1000 ? "MATCH" : "NOT CONFIRMED")+
                "\n\n"+t("Это чтение properties, не памяти OEM-процесса. scale = raw / 1000. FULL RECTANGLE не заменяет визуальную проверку.",
                        "Properties only, not OEM process memory. scale = raw / 1000. FULL RECTANGLE does not replace visual verification."));
    }
    private static String property(String key) {
        try {
            Method get=Class.forName("android.os.SystemProperties").getMethod("get",String.class,String.class);
            Object value=get.invoke(null,key,"?");
            return value==null ? "?" : value.toString();
        } catch (Exception ignored) { return "?"; }
    }
    private WifiManager wifi() { return (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE); }

    private void previewWifi() {
        if (pending) return;
        if (Build.VERSION.SDK_INT!=28) { alert(t("Подключение этим способом разрешено только на Android 9 (API 28). Другие версии не проверены.",
                "This connection workflow is enabled only on Android 9 (API 28). Other versions are untested.")); return; }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this).setMessage(t("Android 9 требует Location permission для чтения SSID. GPS не используется. После выдачи разрешения снова нажмите подключение.",
                    "Android 9 requires Location permission to read the SSID. GPS is not used. Press Connect again after granting permission."))
                    .setNegativeButton(t("Отмена","Cancel"),null)
                    .setPositiveButton("OK",(d,w) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},WIFI_PERMISSION)).show();
            return;
        }
        final String ssid=ssidInput.getText().toString();
        final String password=passwordInput.getText().toString();
        final boolean open=security.getSelectedItemPosition()==1;
        if (!RecoveryRules.validSsid(ssid)) { alert(t("Нужен SSID длиной 1–32 байта UTF-8, без управляющих символов.","SSID must be 1–32 UTF-8 bytes with no control characters.")); return; }
        if (!open && !RecoveryRules.validPsk(password)) { alert(t("Пароль: 8–63 печатных ASCII-символа или 64 hex-цифры PSK.","Password: 8–63 printable ASCII characters or a 64-digit hexadecimal PSK.")); return; }
        new AlertDialog.Builder(this).setTitle(t("Перед изменением Wi-Fi","Before changing Wi-Fi"))
                .setMessage(t("Будет включён Wi-Fi, добавлена/обновлена конфигурация указанной сети и выбран этот SSID вместо текущей сети. Android сохранит конфигурацию. Затем saveConfiguration → enableNetwork → reconnect; ожидание события CONNECTED, SSID и IPv4 до 60 секунд.\n\n",
                        "Wi-Fi will be enabled, this network configuration added/updated and selected instead of the current network. Android will save the configuration. Then saveConfiguration → enableNetwork → reconnect; wait up to 60 seconds for CONNECTED, matching SSID and IPv4.\n\n")+
                        "SSID: "+ssid+"\n"+(open ? t("ОТКРЫТАЯ СЕТЬ: без шифрования.","OPEN NETWORK: unencrypted.") : "WPA/WPA2-PSK")+
                        "\n\n"+t("Zoom/Keystone не изменяются. При неудаче сохранённая сеть может остаться; автоматического отката Wi-Fi нет.",
                        "Zoom/Keystone will not change. On failure, a saved network may remain; there is no automatic Wi-Fi rollback."))
                .setNegativeButton(t("Отмена","Cancel"),(d,w) -> passwordInput.setText(""))
                .setOnCancelListener(d -> passwordInput.setText(""))
                .setPositiveButton(t("Подключить","Connect"),(d,w) -> beginWifi(ssid,password,open)).show();
    }

    private void beginWifi(final String ssid, final String password, final boolean open) {
        passwordInput.setText("");
        try {
            final WifiManager manager=wifi();
            if (manager==null) { alert("WifiManager unavailable"); return; }
            if (!registered) {
                registerReceiver(receiver,new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                registered=true;
            }
            pending=true; pendingSsid=ssid; connectedEvent=false; connectButton.setEnabled(false);
            wifiResult=t("Подключение…","Connecting…"); renderWifi();
            handler.postDelayed(timeout,60000);
            handler.postDelayed(poll,1000);
            boolean enabled=manager.isWifiEnabled();
            if (!enabled && !manager.setWifiEnabled(true)) { fail("setWifiEnabled = false"); return; }
            handler.postDelayed(() -> {
                if (pending) configureWifi(manager,ssid,password,open);
            },enabled ? 0 : 2500);
        } catch (Exception e) { fail(t("Wi-Fi: отказ операции — ","Wi-Fi operation failed — ")+e.getClass().getSimpleName()); }
    }

    private void configureWifi(WifiManager manager,String ssid,String password,boolean open) {
        try {
            if (!manager.isWifiEnabled()) { fail(t("Wi-Fi ещё не включён. Повторите позже.","Wi-Fi is not enabled yet. Retry later.")); return; }
            WifiConfiguration c=new WifiConfiguration();
            c.SSID=RecoveryRules.quote(ssid);
            c.status=WifiConfiguration.Status.ENABLED;
            if (open) c.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            else {
                c.preSharedKey=RecoveryRules.hexPsk(password) ? password : RecoveryRules.quote(password);
                c.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                c.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                c.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                c.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                c.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                c.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }
            int id=-1;
            List<WifiConfiguration> saved=manager.getConfiguredNetworks();
            if (saved!=null) for (WifiConfiguration item:saved)
                if (item!=null && c.SSID.equals(item.SSID)) { id=item.networkId; break; }
            int networkId;
            if (id>=0) { c.networkId=id; networkId=manager.updateNetwork(c); }
            else networkId=manager.addNetwork(c);
            if (networkId<0) { fail("addNetwork/updateNetwork = -1"); return; }
            if (!manager.saveConfiguration()) { fail("saveConfiguration = false"); return; }
            if (!manager.enableNetwork(networkId,true)) { fail("enableNetwork = false"); return; }
            if (!manager.reconnect()) { fail("reconnect = false"); return; }
            wifiResult=t("Команды приняты. Ожидание события CONNECTED и IPv4…","Commands accepted. Waiting for CONNECTED event and IPv4…");
            renderWifi();
        } catch (Exception e) { fail(t("Операция Wi-Fi отклонена — ","Wi-Fi operation rejected — ")+e.getClass().getSimpleName()); }
    }

    private void verifyConnection() {
        if (!pending || !connectedEvent) return;
        try {
            WifiManager manager=wifi();
            WifiInfo info=manager==null ? null : manager.getConnectionInfo();
            if (info==null || info.getIpAddress()==0 || !pendingSsid.equals(RecoveryRules.unquote(info.getSSID()))) return;
            wifiResult=t("Последнее подтверждённое подключение: ","Last confirmed connection: ")+pendingSsid+"\nIPv4: "+RecoveryRules.ipv4(info.getIpAddress())+
                    "\n"+t("Событие NETWORK_STATE_CHANGED_ACTION получено.","NETWORK_STATE_CHANGED_ACTION received.");
            finishPending(); renderWifi();
        } catch (Exception e) { fail(e.getClass().getSimpleName()); }
    }
    private void finishPending() {
        pending=false; pendingSsid=null; connectedEvent=false;
        handler.removeCallbacks(timeout); handler.removeCallbacks(poll);
        connectButton.setEnabled(true);
    }
    private void fail(String reason) {
        finishPending();
        wifiResult="NOT CONFIRMED: "+reason;
        renderWifi();
        alert(reason+"\n"+t("Zoom/Keystone не изменялись. Wi-Fi мог частично измениться; повторное подключение только по вашему подтверждению.",
                "Zoom/Keystone were not changed. Wi-Fi may have partially changed; retry requires your confirmation."));
    }
    private void renderWifi() {
        try {
            WifiManager manager=wifi();
            WifiInfo info=manager==null ? null : manager.getConnectionInfo();
            wifiStatus.setText(wifiResult+"\nWi-Fi enabled: "+(manager!=null && manager.isWifiEnabled())+
                    "\nSSID: "+(info==null ? "?" : info.getSSID())+"\nIPv4: "+(info==null ? "?" : RecoveryRules.ipv4(info.getIpAddress())));
        } catch (Exception e) { wifiStatus.setText(wifiResult+"\nWi-Fi: "+e.getClass().getSimpleName()); }
    }
    private void alert(String s) { new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show(); }
    private TextView addText(LinearLayout root,String value,int size) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(Color.rgb(24,40,64));
        root.addView(v,params()); return v;
    }
    private Button addButton(LinearLayout root,String value,View.OnClickListener listener) {
        Button b=new Button(this); b.setText(value); b.setAllCaps(false); b.setMinHeight(dp(54)); b.setFocusable(true);
        b.setOnClickListener(listener); root.addView(b,params()); return b;
    }
    private LinearLayout.LayoutParams params() {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0,dp(6),0,dp(6)); return p;
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
}

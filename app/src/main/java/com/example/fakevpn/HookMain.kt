package com.example.fakevpn

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.net.NetworkInterface
import java.util.Collections

/**
 * FakeVPN — LSPosed / Xposed 模块核心 Hook 入口
 *
 * 功能：让被 Hook 的目标应用认为当前设备正在使用 VPN。
 *
 * 拦截的 API：
 *  1. NetworkCapabilities.hasTransport(TRANSPORT_VPN)  → true
 *  2. NetworkCapabilities.hasCapability(NET_CAPABILITY_NOT_VPN) → false
 *  3. ConnectivityManager.getActiveNetworkInfo() → type = TYPE_VPN
 *  4. NetworkInterface.getNetworkInterfaces() → 注入假 tun0 接口
 */
class HookMain : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "FakeVPN"
        private const val MODULE_PACKAGE = "com.example.fakevpn"
        private const val MODULE_ACTIVE_CLASS = "com.example.fakevpn.ui.MainActivityKt"
        private const val MODULE_ACTIVE_METHOD = "isXposedModuleActive"

        // android.net.NetworkCapabilities 常量
        private const val TRANSPORT_VPN = 4
        private const val NET_CAPABILITY_NOT_VPN = 15

        // android.net.ConnectivityManager 常量
        private const val TYPE_VPN = 17
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.log("[$TAG] Loaded in: ${lpparam.packageName}")

        if (lpparam.packageName == MODULE_PACKAGE) {
            hookModuleActiveFlag(lpparam)
        }

        hookNetworkCapabilitiesHasTransport(lpparam)
        hookNetworkCapabilitiesHasCapability(lpparam)
        hookGetActiveNetworkInfo(lpparam)
        hookNetworkInterfaceGetAll(lpparam)
    }

    private fun hookModuleActiveFlag(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                MODULE_ACTIVE_CLASS,
                lpparam.classLoader,
                MODULE_ACTIVE_METHOD,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                }
            )
            XposedBridge.log("[$TAG] Hooked module active check")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] Failed to hook module active check: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  1. NetworkCapabilities.hasTransport(int transportType)
    //     当查询 TRANSPORT_VPN (4) 时，强制返回 true
    // ──────────────────────────────────────────────────────────────
    private fun hookNetworkCapabilitiesHasTransport(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.NetworkCapabilities",
                lpparam.classLoader,
                "hasTransport",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val transportType = param.args[0] as Int
                        if (transportType == TRANSPORT_VPN) {
                            param.result = true
                            XposedBridge.log("[$TAG] hasTransport(TRANSPORT_VPN) → true")
                        }
                    }
                }
            )
            XposedBridge.log("[$TAG] ✓ Hooked NetworkCapabilities.hasTransport()")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ✗ Failed to hook hasTransport: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  2. NetworkCapabilities.hasCapability(int capability)
    //     当查询 NET_CAPABILITY_NOT_VPN (15) 时，强制返回 false
    //     表示"没有 NOT_VPN 能力" → 即"是 VPN"
    // ──────────────────────────────────────────────────────────────
    private fun hookNetworkCapabilitiesHasCapability(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.NetworkCapabilities",
                lpparam.classLoader,
                "hasCapability",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val capability = param.args[0] as Int
                        if (capability == NET_CAPABILITY_NOT_VPN) {
                            param.result = false
                            XposedBridge.log("[$TAG] hasCapability(NET_CAPABILITY_NOT_VPN) → false")
                        }
                    }
                }
            )
            XposedBridge.log("[$TAG] ✓ Hooked NetworkCapabilities.hasCapability()")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ✗ Failed to hook hasCapability: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  3. ConnectivityManager.getActiveNetworkInfo()
    //     将返回值的 type 字段修改为 TYPE_VPN (17)
    // ──────────────────────────────────────────────────────────────
    @Suppress("DEPRECATION")
    private fun hookGetActiveNetworkInfo(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.ConnectivityManager",
                lpparam.classLoader,
                "getActiveNetworkInfo",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val networkInfo = param.result ?: return
                        try {
                            // NetworkInfo.mNetworkType 是 private int 字段
                            XposedHelpers.setIntField(networkInfo, "mNetworkType", TYPE_VPN)
                            XposedHelpers.setObjectField(networkInfo, "mTypeName", "VPN")
                            XposedBridge.log("[$TAG] getActiveNetworkInfo().type → TYPE_VPN")
                        } catch (e: Throwable) {
                            XposedBridge.log("[$TAG] ✗ Failed to modify NetworkInfo: ${e.message}")
                        }
                    }
                }
            )
            XposedBridge.log("[$TAG] ✓ Hooked ConnectivityManager.getActiveNetworkInfo()")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ✗ Failed to hook getActiveNetworkInfo: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  4. NetworkInterface.getNetworkInterfaces()
    //     在返回的枚举中注入一个假的 tun0 接口
    // ──────────────────────────────────────────────────────────────
    private fun hookNetworkInterfaceGetAll(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "java.net.NetworkInterface",
                lpparam.classLoader,
                "getNetworkInterfaces",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        @Suppress("UNCHECKED_CAST")
                        val original = param.result as? java.util.Enumeration<NetworkInterface> ?: return

                        val list = Collections.list(original).toMutableList()

                        // 如果已经存在 tun0 就不需要再注入
                        val hasTun = list.any { iface ->
                            iface.name.startsWith("tun") || iface.name.startsWith("ppp")
                        }

                        if (!hasTun) {
                            try {
                                // 通过反射创建一个假的 NetworkInterface
                                val fakeTun = createFakeNetworkInterface("tun0", 100)
                                if (fakeTun != null) {
                                    list.add(fakeTun)
                                    XposedBridge.log("[$TAG] Injected fake tun0 interface")
                                }
                            } catch (e: Throwable) {
                                XposedBridge.log("[$TAG] ✗ Failed to inject tun0: ${e.message}")
                            }
                        }

                        param.result = Collections.enumeration(list)
                    }
                }
            )
            XposedBridge.log("[$TAG] ✓ Hooked NetworkInterface.getNetworkInterfaces()")
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ✗ Failed to hook getNetworkInterfaces: ${e.message}")
        }
    }

    /**
     * 利用反射创建一个假的 NetworkInterface 实例
     */
    private fun createFakeNetworkInterface(name: String, index: Int): NetworkInterface? {
        return try {
            val constructor = NetworkInterface::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val fakeInterface = constructor.newInstance()

            // 设置 name 字段
            val nameField = NetworkInterface::class.java.getDeclaredField("name")
            nameField.isAccessible = true
            nameField.set(fakeInterface, name)

            // 设置 index 字段
            val indexField = NetworkInterface::class.java.getDeclaredField("index")
            indexField.isAccessible = true
            indexField.setInt(fakeInterface, index)

            fakeInterface
        } catch (e: Throwable) {
            XposedBridge.log("[$TAG] ✗ createFakeNetworkInterface error: ${e.message}")
            null
        }
    }
}

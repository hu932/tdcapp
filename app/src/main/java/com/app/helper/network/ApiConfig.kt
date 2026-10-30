package com.app.helper.network

object ApiConfig {
    // ===== 我们的后端：只负责指纹环境下发 =====
    // 手机号即账号，后端做白名单授权 + 每日额度
    const val BACKEND_BASE_URL = "http://38.175.194.43"
    const val FP_ENV_GENERATE_PATH = "/api/fp_env/generate"
    val FP_ENV_URL: String get() = BACKEND_BASE_URL + FP_ENV_GENERATE_PATH

    // ===== 任务平台接口（保持原样，不走我们的后端）=====
    const val TEAM_BASE_URL = "http://43.128.97.191:3008"
    const val TEAM_TASK_TAKE_PATH = "/api/task"          // GET ?phone=xxx
    const val TEAM_TASK_UPLOAD_PATH = "/task/upload/json" // POST

    // ===== 解密接口（后台 Go 服务，?act=decrypt_test） =====
    const val DECRYPT_URL = "http://103.146.231.72:2238/?act=decrypt_test"

    // ===== 旧代理地址（更新检查 / 指纹上报 / 风控上报等仍走这里） =====
    const val DECRYPT_PROXY_URL = "http://103.146.231.72:2238/decrypt_proxy.php"

    const val TARGET_PACKAGE = "com.shopee.tw"
    const val WAKE_ACTION = "com.oppo.wake.launch.action"

    const val CONTENT_PROVIDER_AUTHORITY = "com.yh.csyxgl.vivo.provider"
    const val CONTENT_PROVIDER_PATH_CURRENT = "current_fp"
    const val CONTENT_PROVIDER_PATH_FINGERPRINT_CURRENT = "fingerprint/current"
}

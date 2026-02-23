// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // 使用 alias 引用 toml 中定义的插件
    alias(libs.plugins.dagger.hilt.android) apply false
    // 如果你打算用 KSP 替代 Kapt，也要在这里声明
    alias(libs.plugins.google.devtools.ksp) apply false
}
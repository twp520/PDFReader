/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ncw6fg.nxhw18e.pdfreader.money

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.ads.nativead.NativeAd


// [START display_native_ad]
@Composable
fun DisplayNativeAdView(nativeAd: NativeAd) {
    Box(
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(8.dp)
    ) {
        // Call the NativeAdView composable to display the native ad.
        NativeAdView(nativeAd) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // If available, display the icon asset.
                        nativeAd.icon?.let { icon ->
                            NativeAdIconView(Modifier.padding(5.dp)) {
                                icon.drawable?.toBitmap()?.let { bitmap ->
                                    Image(bitmap = bitmap.asImageBitmap(), "Icon")
                                }
                            }
                        }
                        Column {
                            // If available, display the headline asset.
                            nativeAd.headline?.let {
                                NativeAdHeadlineView {
                                    Text(text = it, style = MaterialTheme.typography.headlineLarge)
                                }
                            }
                            // If available, display the star rating asset.
                            nativeAd.starRating?.let {
                                NativeAdStarRatingView {
                                    Text(
                                        text = "Rated $it",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                    // Display the ad attribution.
                    NativeAdAttribution(
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }

                // Display the media asset.
                NativeAdMediaView(modifier = Modifier.fillMaxWidth())

                // If available, display the body asset.
                nativeAd.body?.let {
                    NativeAdBodyView(modifier = Modifier.padding(5.dp)) { Text(text = it) }
                }

                Row(
                    Modifier
                        .align(Alignment.End)
                        .padding(5.dp)
                ) {
                    // If available, display the price asset.
                    nativeAd.price?.let {
                        NativeAdPriceView(
                            Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(text = it)
                        }
                    }
                    // If available, display the store asset.
                    nativeAd.store?.let {
                        NativeAdStoreView(
                            Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(text = it)
                        }
                    }
                    // If available, display the call to action asset.
                    nativeAd.callToAction?.let { callToAction ->
                        NativeAdCallToActionView(Modifier.padding(5.dp)) { NativeAdButton(text = callToAction) }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplaySmallNativeAdView(nativeAd: NativeAd) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp, MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // 核心包装容器
        NativeAdView(nativeAd) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // 1. 中间内容区：左侧图标 + 右侧文字信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧图标 (NativeAdIconView)
                    nativeAd.icon?.let { icon ->
                        NativeAdIconView(
                            modifier = Modifier
                                .size(60.dp)
                        ) {
                            icon.drawable?.toBitmap()?.let { bitmap ->
                                Image(bitmap = bitmap.asImageBitmap(), "Icon")
                            }
                        }
                    }

                    // 右侧文字信息列
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        // 标题 (NativeAdHeadlineView)
                        nativeAd.headline?.let {
                            NativeAdHeadlineView {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                )
                            }
                        }

                        // 广告标识 + 评分 (次要信息行)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // 自定义“广告”标签
                            NativeAdAttribution()

                            Spacer(modifier = Modifier.width(8.dp))

                            // 评分 (NativeAdStarRatingView)
                            nativeAd.starRating?.let { rating ->
                                NativeAdStarRatingView {
                                    Text(
                                        text = "Rated $rating",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // 描述正文 (NativeAdBodyView)
                        nativeAd.body?.let {
                            NativeAdBodyView {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 2. 底部行动按钮 (CTA)
                nativeAd.callToAction?.let { ctaText ->
                    NativeAdCallToActionView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        NativeAdButton(text = ctaText)
                    }
                }
            }
        }
    }
}


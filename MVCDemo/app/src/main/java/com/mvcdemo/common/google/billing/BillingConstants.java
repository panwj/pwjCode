/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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
package com.mvcdemo.common.google.billing;

import com.android.billingclient.api.BillingClient.SkuType;

import java.util.Arrays;
import java.util.List;

/**
 * Static fields and methods useful for billing
 */
public final class BillingConstants {
    // SKUs for our products: oldVersion for purchase remove ads
    public static final String SKU_PURCHASE_NO_ADS = "sr_pro_no_ads";

    // SKU for our subscription (infinite gas)
    public static final String SKU_NO_ADS_MONTHLY = "sub_month";
    public static final String SKU_NO_ADS_YEARLY = "sub_year";

    //SKU for our subscription(Modify the price)
    public static final String SKU_PRO_MONTHLY = "sub_month_new";
    public static final String SKU_PRO_YEARLY = "sub_year_new";

    public static final String SKU_PRO_MONTHLY_NEW = "sub_month_new_user";
    public static final String SKU_PRO_YEARLY_NEW = "sub_year_new_user";

    private static final String[] IN_APP_SKUS = {SKU_PURCHASE_NO_ADS};
    private static final String[] SUBSCRIPTIONS_SKUS = {SKU_NO_ADS_MONTHLY, SKU_NO_ADS_YEARLY, SKU_PRO_MONTHLY, SKU_PRO_YEARLY, SKU_PRO_MONTHLY_NEW, SKU_PRO_YEARLY_NEW};

    private BillingConstants(){}

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    public static final List<String> getSkuList(@SkuType String billingType) {
        return (billingType == SkuType.INAPP) ? Arrays.asList(IN_APP_SKUS)
                : Arrays.asList(SUBSCRIPTIONS_SKUS);
    }
}


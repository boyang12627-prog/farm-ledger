package com.farmledger.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LedgerCategoryTest {
    @Test
    fun farmObjectMapping_locked() {
        assertThat(LedgerCategory.FOOD.farmObjectZh).contains("餐桌")
        assertThat(LedgerCategory.TRANSPORT.farmObjectZh).contains("小路")
        assertThat(LedgerCategory.HOUSING.farmObjectZh).contains("門廊")
        assertThat(LedgerCategory.DAILY.farmObjectZh).contains("木箱")
        assertThat(LedgerCategory.ENTERTAINMENT.farmObjectZh).contains("花圃")
        assertThat(LedgerCategory.HEALTH.farmObjectZh).contains("藥草")
        assertThat(LedgerCategory.INCOME.farmObjectZh).contains("郵箱")
        assertThat(LedgerCategory.SAVINGS.farmObjectZh).contains("撲滿")
        assertThat(LedgerCategory.OTHER.farmObjectZh).contains("告示牌")
    }

    @Test
    fun fromStorage_acceptsNameOrZh() {
        assertThat(LedgerCategory.fromStorage("FOOD")).isEqualTo(LedgerCategory.FOOD)
        assertThat(LedgerCategory.fromStorage("飲食")).isEqualTo(LedgerCategory.FOOD)
        assertThat(LedgerCategory.fromStorage(null)).isNull()
        assertThat(LedgerCategory.fromStorage("")).isNull()
    }
}

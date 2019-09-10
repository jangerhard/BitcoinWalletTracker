package io.github.jangerhard.BitcoinWalletTracker.utilities

import io.mockk.every
import io.mockk.mockk
import io.vavr.collection.List
import io.vavr.control.Option
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BitcoinUtilsTest {

    private val preferences = mockk<SharedPreferencesHelper>()

    private lateinit var bitcoinUtils: BitcoinUtils

    @Before
    fun setup() {
        every { preferences.getCurrency() } returns Option.some("USD")
        every { preferences.getAddresses() } returns List.of("1LVuX2MLwerH6sFb25HnyCFS8Zcuxc2u1s", "1H6a4TidysCEV91PDdQZmyEphpJD9M7VmN")
        bitcoinUtils = BitcoinUtils(preferences)
    }

    @Test
    fun `getting wallets from prefs given existing addresses returns list`() {
        assertFalse(bitcoinUtils.addressesFromPrefs.isEmpty)
    }

    @Test
    fun `getting wallets from prefs given no existing addresses returns list`() {
        every { preferences.getAddresses() } returns List.empty()
        assertTrue(bitcoinUtils.addressesFromPrefs.isEmpty)
    }

    @Test
    fun `test Formating Balance To String`() {

        var bal = BitcoinUtils.formatBitcoinBalanceToString(2195820L)
        assertEquals("21.9582 mBTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(21958200L)
        assertEquals("0.2196 BTC", bal)
        bal = BitcoinUtils.formatBitcoinBalanceToString(219582000L)
        assertEquals("2.1958 BTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(0L)
        assertEquals("0.0000 mBTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(3428272196L)
        assertEquals("34.2827 BTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(541721794L)
        assertEquals("5.4172 BTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(-3428272196L)
        assertEquals("-34.2827 BTC", bal)

        bal = BitcoinUtils.formatBitcoinBalanceToString(-541721794L)
        assertEquals("-5.4172 BTC", bal)
    }

    @Test
    fun `given correct bitcoin address return BITCOIN address`() {

        // Real address, should return true
        var maybeVerified: Option<BitcoinUtils.ACCOUNT_TYPE> = BitcoinUtils.verifyAddress("1LVuX2MLwerH6sFb25HnyCFS8Zcuxc2u1s")
        assertTrue(maybeVerified.isDefined)
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.BITCOIN, maybeVerified.get())

        // Real address, should return true
        maybeVerified = BitcoinUtils.verifyAddress("1H6a4TidysCEV91PDdQZmyEphpJD9M7VmN")
        assertTrue(maybeVerified.isDefined)
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.BITCOIN, maybeVerified.get())
    }

    @Test
    fun `given correct segwit address using xpub return SEGWIT address`() {
        // Real address, should return true
        val maybeVerified = BitcoinUtils.verifyAddress("xpub68KFnj3bqUx1s7mHejLDBPywCAKdJEu1b49uniEEn2WSbHmZ7xbLqFTjJbtx1LUcAt1DwhoqWHmo2s5WMJp6wi38CiF2hYD49qVViKVvAoi")
        assertTrue(maybeVerified.isDefined)
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.SEGWIT, maybeVerified.get())
    }

    @Test
    fun `given correct segwit address using ypub return SEGWIT address`() {
        // Real address, should return true
        val maybeVerified = BitcoinUtils.verifyAddress("ypub6QqdH2c5z79681jUgdxjGJzGW9zpL4ryPCuhtZE4GpvrJoZqM823XQN6iSQeVbbbp2uCRQ9UgpeMcwiyV6qjvxTWVcxDn2XEAnioMUwsrQ5")
        assertTrue(maybeVerified.isDefined)
        assertEquals(BitcoinUtils.ACCOUNT_TYPE.SEGWIT, maybeVerified.get())
    }

    @Test
    fun `given wrong address return none`() {

        // Wrong address, should return false
        var maybeVerified: Option<BitcoinUtils.ACCOUNT_TYPE> = BitcoinUtils.verifyAddress("randobambo")
        assertFalse(maybeVerified.isDefined)
        // Wrong address, should return false
        maybeVerified = BitcoinUtils.verifyAddress("43ghcYT1EKNKUzgGaXnv8qty16mifc8iy2")
        assertFalse(maybeVerified.isDefined)

        maybeVerified = BitcoinUtils.verifyAddress("")
        assertFalse(maybeVerified.isDefined)

        maybeVerified = BitcoinUtils.verifyAddress(null)
        assertFalse(maybeVerified.isDefined)
    }

}

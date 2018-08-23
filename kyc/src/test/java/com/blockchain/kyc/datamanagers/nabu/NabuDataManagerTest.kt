package com.blockchain.kyc.datamanagers.nabu

import com.blockchain.kyc.models.nabu.NabuCountryResponse
import com.blockchain.kyc.models.nabu.NabuOfflineTokenResponse
import com.blockchain.kyc.models.nabu.NabuSessionTokenResponse
import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.Scope
import com.blockchain.kyc.models.nabu.UserId
import com.blockchain.kyc.services.nabu.NabuService
import com.blockchain.kyc.stores.NabuSessionTokenStore
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.api.data.Settings
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.settings.SettingsDataManager
import piuk.blockchain.androidcore.utils.Optional

class NabuDataManagerTest {

    private lateinit var subject: NabuDataManager
    private val nabuService: NabuService = mock()
    private val nabuTokenStore: NabuSessionTokenStore = mock()
    private val settingsDataManager: SettingsDataManager = mock()
    private val payloadDataManager: PayloadDataManager = mock()
    private val appVersion = "6.14.0"
    private val deviceId = "DEVICE_ID"
    private val email = "EMAIL"
    private val guid = "GUID"

    @Before
    fun setUp() {
        whenever(payloadDataManager.guid).thenReturn(guid)

        val settings: Settings = mock()
        whenever(settings.email).thenReturn(email)
        whenever(settingsDataManager.getSettings()).thenReturn(Observable.just(settings))

        subject = NabuDataManager(
            nabuService,
            nabuTokenStore,
            appVersion,
            deviceId,
            settingsDataManager,
            payloadDataManager
        )
    }

    @Test
    fun createUser() {
        // Arrange
        val userId = "USER_ID"
        whenever(
            nabuService.createUserId(
                guid = guid,
                email = email
            )
        ).thenReturn(Single.just(UserId(userId)))
        // Act
        val testObserver = subject.createUserId().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(userId)
        verify(nabuService).createUserId(
            guid = guid,
            email = email
        )
    }

    @Test
    fun getAuthToken() {
        // Arrange
        val userId = "USER_ID"
        val token = "TOKEN"
        val tokenResponse = NabuOfflineTokenResponse(userId, token)
        whenever(
            nabuService.getAuthToken(
                guid = guid,
                email = email,
                userId = userId,
                deviceId = deviceId,
                appVersion = appVersion
            )
        ).thenReturn(Single.just(tokenResponse))
        // Act
        val testObserver = subject.getAuthToken(userId).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(tokenResponse)
        verify(nabuService).getAuthToken(
            guid = guid,
            email = email,
            userId = userId,
            deviceId = deviceId,
            appVersion = appVersion
        )
    }

    @Test
    fun getSessionToken() {
        // Arrange
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionTokenResponse = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(
            nabuService.getSessionToken(
                guid = guid,
                email = email,
                offlineToken = offlineToken.token,
                userId = offlineToken.userId,
                deviceId = deviceId,
                appVersion = appVersion
            )
        ).thenReturn(Single.just(sessionTokenResponse))
        // Act
        val testObserver =
            subject.getSessionToken(offlineToken).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(sessionTokenResponse)
        verify(nabuService).getSessionToken(
            guid = guid,
            email = email,
            offlineToken = offlineToken.token,
            userId = offlineToken.userId,
            deviceId = deviceId,
            appVersion = appVersion
        )
    }

    @Test
    fun createBasicUser() {
        // Arrange
        val firstName = "FIRST_NAME"
        val lastName = "LAST_NAME"
        val dateOfBirth = "25-02-1995"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(
            nabuService.createBasicUser(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                sessionToken = sessionToken.token
            )
        ).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.createBasicUser(
            firstName,
            lastName,
            dateOfBirth,
            offlineToken
        ).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).createBasicUser(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            sessionToken = sessionToken.token
        )
    }

    @Test
    fun getUser() {
        // Arrange
        val userObject: NabuUser = mock()
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(
            nabuService.getUser(sessionToken = sessionToken.token)
        ).thenReturn(Single.just(userObject))
        // Act
        val testObserver = subject.getUser(offlineToken).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(userObject)
        verify(nabuService).getUser(sessionToken = sessionToken.token)
    }

    @Test
    fun addAddress() {
        // Arrange
        val city = "CITY"
        val line1 = "LINE1"
        val line2 = "LINE2"
        val state = null
        val countryCode = "COUNTRY_CODE"
        val postCode = "POST_CODE"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(
            nabuService.addAddress(
                sessionToken = sessionToken.token,
                line1 = line1,
                line2 = line2,
                city = city,
                state = state,
                postCode = postCode,
                countryCode = countryCode
            )
        ).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.addAddress(
            offlineToken,
            line1,
            line2,
            city,
            state,
            postCode,
            countryCode
        ).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).addAddress(
            sessionToken = sessionToken.token,
            line1 = line1,
            line2 = line2,
            city = city,
            state = state,
            postCode = postCode,
            countryCode = countryCode
        )
    }

    @Test
    fun addMobileNumber() {
        // Arrange
        val mobileNumber = "MOBILE_NUMBER"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(
            nabuService.addMobileNumber(
                mobileNumber = mobileNumber,
                sessionToken = sessionToken.token
            )
        ).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.addMobileNumber(
            offlineToken,
            mobileNumber
        ).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).addMobileNumber(
            mobileNumber = mobileNumber,
            sessionToken = sessionToken.token
        )
    }

    @Test
    fun verifyMobileNumber() {
        // Arrange
        val mobileNumber = "MOBILE_NUMBER"
        val verificationCode = "VERIFICATION_CODE"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(
            nabuService.verifyMobileNumber(
                mobileNumber = mobileNumber,
                verificationCode = verificationCode,
                sessionToken = sessionToken.token
            )
        ).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.verifyMobileNumber(
            offlineToken,
            mobileNumber,
            verificationCode
        ).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).verifyMobileNumber(
            mobileNumber = mobileNumber,
            verificationCode = verificationCode,
            sessionToken = sessionToken.token
        )
    }

    @Test
    fun getCountriesList() {
        // Arrange
        val countriesList = listOf(
            NabuCountryResponse("GER", "Germany", listOf("EEA"), listOf("KYC")),
            NabuCountryResponse("UK", "United Kingdom", listOf("EEA"), listOf("KYC"))
        )
        whenever(nabuService.getCountriesList(scope = Scope.Kyc))
            .thenReturn(Single.just(countriesList))
        // Act
        val testObserver = subject.getCountriesList(Scope.Kyc).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(countriesList)
        verify(nabuService).getCountriesList(scope = Scope.Kyc)
    }

    @Test
    fun getOnfidoApiKey() {
        // Arrange
        val apiKey = "API_KEY"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(nabuService.getOnfidoApiKey(sessionToken = sessionToken.token))
            .thenReturn(Single.just(apiKey))
        // Act
        val testObserver = subject.getOnfidoApiKey(offlineToken).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).getOnfidoApiKey(sessionToken = sessionToken.token)
    }

    @Test
    fun submitOnfidoVerification() {
        // Arrange
        val applicantId = "APPLICATION_ID"
        val offlineToken = NabuOfflineTokenResponse("", "")
        val sessionToken = NabuSessionTokenResponse("", "", "", true, "", "", "")
        whenever(nabuTokenStore.requiresRefresh()).thenReturn(false)
        whenever(nabuTokenStore.getAccessToken())
            .thenReturn(Observable.just(Optional.Some(sessionToken)))
        whenever(nabuService.submitOnfidoVerification(
            sessionToken = sessionToken.token,
            applicantId = applicantId
        )).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.submitOnfidoVerification(offlineToken, applicantId).test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(nabuService).submitOnfidoVerification(
            sessionToken = sessionToken.token,
            applicantId = applicantId
        )
    }
}
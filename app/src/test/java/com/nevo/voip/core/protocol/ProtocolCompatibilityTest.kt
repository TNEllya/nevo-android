package com.nevo.voip.core.protocol

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.nevo.voip.core.model.*

class ProtocolCompatibilityTest {

    // ================================================================
    // Primitive Type Tests
    // ================================================================

    @Test
    fun `NevoBuffer write and read u16`() {
        val buf = NevoBuffer()
        buf.writeU16(65535)
        buf.writeU16(0)
        buf.writeU16(256)

        val reader = NevoBuffer(buf.toByteArray())
        assertEquals(65535, reader.readU16())
        assertEquals(0, reader.readU16())
        assertEquals(256, reader.readU16())
    }

    @Test
    fun `NevoBuffer write and read u32`() {
        val buf = NevoBuffer()
        buf.writeU32(Int.MAX_VALUE.toInt())
        buf.writeU32(0)
        buf.writeU32(42)

        val reader = NevoBuffer(buf.toByteArray())
        assertEquals(Int.MAX_VALUE.toInt(), reader.readU32())
        assertEquals(0, reader.readU32())
        assertEquals(42, reader.readU32())
    }

    @Test
    fun `NevoBuffer write and read u64`() {
        val buf = NevoBuffer()
        buf.writeU64(Long.MAX_VALUE)
        buf.writeU64(0L)
        buf.writeU64(1234567890123L)

        val reader = NevoBuffer(buf.toByteArray())
        assertEquals(Long.MAX_VALUE, reader.readU64())
        assertEquals(0L, reader.readU64())
        assertEquals(1234567890123L, reader.readU64())
    }

    @Test
    fun `NevoBuffer write and read bool`() {
        val buf = NevoBuffer()
        buf.writeBool(true)
        buf.writeBool(false)
        buf.writeBool(true)

        val reader = NevoBuffer(buf.toByteArray())
        assertTrue(reader.readBool())
        assertFalse(reader.readBool())
        assertTrue(reader.readBool())
    }

    @Test
    fun `NevoBuffer write and read string`() {
        val buf = NevoBuffer()
        buf.writeString("Hello, 世界! 🌍")
        buf.writeString("")
        buf.writeString("test")

        val reader = NevoBuffer(buf.toByteArray())
        assertEquals("Hello, 世界! 🌍", reader.readString())
        assertEquals("", reader.readString())
        assertEquals("test", reader.readString())
    }

    @Test
    fun `NevoBuffer write and read bytes`() {
        val data1 = byteArrayOf(1, 2, 3, 4, 5)
        val data2 = ByteArray(0)
        val data3 = ByteArray(256) { it.toByte() }

        val buf = NevoBuffer()
        buf.writeBytes(data1)
        buf.writeBytes(data2)
        buf.writeBytes(data3)

        val reader = NevoBuffer(buf.toByteArray())
        assertArrayEquals(data1, reader.readBytes())
        assertArrayEquals(data2, reader.readBytes())
        assertArrayEquals(data3, reader.readBytes())
    }

    // ================================================================
    // UserInfo Roundtrip Tests
    // ================================================================

    @Test
    fun `UserInfo roundtrip serialization`() {
        val original = UserInfo(
            id = 42L,
            username = "TestUser",
            status = PbUserStatus.ONLINE.value,
            muted = false,
            deafened = true,
            groupId = 7
        )
        val bytes = ProtocolSerializer.serializeUserInfo(original)
        val restored = ProtocolSerializer.deserializeUserInfo(bytes)

        assertEquals(original.id, restored.id)
        assertEquals(original.username, restored.username)
        assertEquals(original.status, restored.status)
        assertEquals(original.muted, restored.muted)
        assertEquals(original.deafened, restored.deafened)
        assertEquals(original.groupId, restored.groupId)
    }

    // ================================================================
    // ChannelInfo Roundtrip Tests
    // ================================================================

    @Test
    fun `ChannelInfo roundtrip with nested children`() {
        val childChannel = ChannelInfo(
            id = 2L,
            name = "Sub Channel",
            parentId = 1L,
            children = emptyList(),
            users = listOf(
                UserInfo(id = 100L, username = "Alice", status = PbUserStatus.ONLINE.value, muted = false, deafened = false, groupId = 0)
            )
        )
        val original = ChannelInfo(
            id = 1L,
            name = "Main Channel",
            parentId = 0L,
            children = listOf(childChannel),
            users = listOf(
                UserInfo(id = 99L, username = "Bob", status = PbUserStatus.ONLINE.value, muted = true, deafened = false, groupId = 1)
            )
        )

        val bytes = ProtocolSerializer.serializeChannelInfo(original)
        val restored = ProtocolSerializer.deserializeChannelInfo(bytes)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.parentId, restored.parentId)
        assertEquals(original.children.size, restored.children.size)
        assertEquals(original.children[0].id, restored.children[0].id)
        assertEquals(original.children[0].name, restored.children[0].name)
        assertEquals(original.users.size, restored.users.size)
    }

    @Test
    fun `ChannelInfo roundtrip empty channel`() {
        val original = ChannelInfo(
            id = 1L,
            name = "Empty",
            parentId = 0L,
            children = emptyList(),
            users = emptyList()
        )
        val bytes = ProtocolSerializer.serializeChannelInfo(original)
        val restored = ProtocolSerializer.deserializeChannelInfo(bytes)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertTrue(restored.children.isEmpty())
        assertTrue(restored.users.isEmpty())
    }

    // ================================================================
    // Login Messages
    // ================================================================

    @Test
    fun `LoginRequest roundtrip`() {
        val original = LoginRequest(
            username = "test_user",
            authCredential = "password123".toByteArray(),
            keyExchangeMethods = listOf("X25519+crypto_box_seal", "X25519"),
            clientPublicKey = ByteArray(32) { it.toByte() },
            clientUdpPort = 12345,
            clientVideoUdpPort = 0
        )
        val bytes = ProtocolSerializer.serializeLoginRequest(original)
        val restored = ProtocolSerializer.deserializeLoginRequest(bytes)

        assertEquals(original.username, restored.username)
        assertArrayEquals(original.authCredential, restored.authCredential)
        assertEquals(original.keyExchangeMethods, restored.keyExchangeMethods)
        assertArrayEquals(original.clientPublicKey, restored.clientPublicKey)
        assertEquals(original.clientUdpPort, restored.clientUdpPort)
    }

    @Test
    fun `LoginRequest with video port roundtrip`() {
        val original = LoginRequest(
            username = "test_user",
            authCredential = "password123".toByteArray(),
            keyExchangeMethods = listOf("X25519"),
            clientPublicKey = ByteArray(32),
            clientUdpPort = 8080,
            clientVideoUdpPort = 8081
        )
        val bytes = ProtocolSerializer.serializeLoginRequest(original)
        val restored = ProtocolSerializer.deserializeLoginRequest(bytes)

        assertEquals(original.clientUdpPort, restored.clientUdpPort)
        assertEquals(original.clientVideoUdpPort, restored.clientVideoUdpPort)
    }

    @Test
    fun `LoginResponse roundtrip`() {
        val userInfo = UserInfo(id = 1L, username = "admin", status = PbUserStatus.ONLINE.value, muted = false, deafened = false, groupId = 0)
        val original = LoginResponse(
            result = ResultCode.OK.value,
            userInfo = userInfo,
            sessionToken = "abc123token",
            serverPublicKey = ByteArray(32) { it.toByte() },
            keyExchangeMethod = "X25519",
            encryptedSessionKey = ByteArray(64),
            ownerExists = 1,
            serverUdpPort = 24431,
            serverVideoUdpPort = 24432
        )
        val bytes = ProtocolSerializer.serializeLoginResponse(original)
        val restored = ProtocolSerializer.deserializeLoginResponse(bytes)

        assertEquals(original.result, restored.result)
        assertEquals(original.userInfo?.id, restored.userInfo?.id)
        assertEquals(original.userInfo?.username, restored.userInfo?.username)
        assertEquals(original.sessionToken, restored.sessionToken)
        assertArrayEquals(original.serverPublicKey, restored.serverPublicKey)
        assertEquals(original.keyExchangeMethod, restored.keyExchangeMethod)
        assertEquals(original.ownerExists, restored.ownerExists)
        assertEquals(original.serverUdpPort, restored.serverUdpPort)
        assertEquals(original.serverVideoUdpPort, restored.serverVideoUdpPort)
    }

    @Test
    fun `LoginResponse without user info roundtrip`() {
        val original = LoginResponse(
            result = ResultCode.ERROR_AUTH_FAILED.value,
            userInfo = null,
            sessionToken = "",
            serverPublicKey = ByteArray(0),
            keyExchangeMethod = "",
            encryptedSessionKey = ByteArray(0),
            ownerExists = 0,
            serverUdpPort = 0,
            serverVideoUdpPort = 0
        )
        val bytes = ProtocolSerializer.serializeLoginResponse(original)
        val restored = ProtocolSerializer.deserializeLoginResponse(bytes)

        assertEquals(original.result, restored.result)
        assertNull(restored.userInfo)
        assertEquals("", restored.sessionToken)
    }

    // ================================================================
    // Channel Management Messages
    // ================================================================

    @Test
    fun `JoinChannelRequest roundtrip`() {
        val original = JoinChannelRequest(channelId = 42L)
        val bytes = ProtocolSerializer.serializeJoinChannelRequest(original)
        val restored = ProtocolSerializer.deserializeJoinChannelRequest(bytes)
        assertEquals(original.channelId, restored.channelId)
    }

    @Test
    fun `LeaveChannelRequest roundtrip`() {
        val original = LeaveChannelRequest()
        val bytes = ProtocolSerializer.serializeLeaveChannelRequest(original)
        val restored = ProtocolSerializer.deserializeLeaveChannelRequest(bytes)
        assertNotNull(restored)
    }

    @Test
    fun `CreateChannelRequest roundtrip`() {
        val original = CreateChannelRequest(parentId = 1L, name = "New Channel")
        val bytes = ProtocolSerializer.serializeCreateChannelRequest(original)
        val restored = ProtocolSerializer.deserializeCreateChannelRequest(bytes)
        assertEquals(original.parentId, restored.parentId)
        assertEquals(original.name, restored.name)
    }

    @Test
    fun `RenameChannelRequest roundtrip`() {
        val original = RenameChannelRequest(channelId = 5L, newName = "Renamed Channel")
        val bytes = ProtocolSerializer.serializeRenameChannelRequest(original)
        val restored = ProtocolSerializer.deserializeRenameChannelRequest(bytes)
        assertEquals(original.channelId, restored.channelId)
        assertEquals(original.newName, restored.newName)
    }

    // ================================================================
    // Chat Messages
    // ================================================================

    @Test
    fun `ChatSendRequest roundtrip`() {
        val original = ChatSendRequest(channelId = 1, text = "Hello, World! 🎮")
        val bytes = ProtocolSerializer.serializeChatSendRequest(original)
        val restored = ProtocolSerializer.deserializeChatSendRequest(bytes)
        assertEquals(original.channelId, restored.channelId)
        assertEquals(original.text, restored.text)
    }

    @Test
    fun `ChatBroadcast roundtrip`() {
        val original = ChatBroadcast(
            senderId = 42L,
            senderName = "Player1",
            channelId = 1,
            text = "Anyone up for a game?",
            timestamp = 1234567890
        )
        val bytes = ProtocolSerializer.serializeChatBroadcast(original)
        val restored = ProtocolSerializer.deserializeChatBroadcast(bytes)
        assertEquals(original.senderId, restored.senderId)
        assertEquals(original.senderName, restored.senderName)
        assertEquals(original.channelId, restored.channelId)
        assertEquals(original.text, restored.text)
        assertEquals(original.timestamp, restored.timestamp)
    }

    // ================================================================
    // Admin Messages
    // ================================================================

    @Test
    fun `AdminAuthRequest roundtrip`() {
        val original = AdminAuthRequest(password = "admin_secret")
        val bytes = ProtocolSerializer.serializeAdminAuthRequest(original)
        val restored = ProtocolSerializer.deserializeAdminAuthRequest(bytes)
        assertEquals(original.password, restored.password)
    }

    @Test
    fun `KickUserRequest roundtrip`() {
        val original = KickUserRequest(userId = 99L, reason = "Spamming")
        val bytes = ProtocolSerializer.serializeKickUserRequest(original)
        val restored = ProtocolSerializer.deserializeKickUserRequest(bytes)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.reason, restored.reason)
    }

    @Test
    fun `BanUserRequest roundtrip`() {
        val original = BanUserRequest(userId = 99L, reason = "Toxic behavior", expiresAt = 1234567890)
        val bytes = ProtocolSerializer.serializeBanUserRequest(original)
        val restored = ProtocolSerializer.deserializeBanUserRequest(bytes)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.reason, restored.reason)
        assertEquals(original.expiresAt, restored.expiresAt)
    }

    // ================================================================
    // Voice Messages
    // ================================================================

    @Test
    fun `UserSpeaking roundtrip`() {
        val original = UserSpeaking(userId = 42L, speaking = true)
        val bytes = ProtocolSerializer.serializeUserSpeaking(original)
        val restored = ProtocolSerializer.deserializeUserSpeaking(bytes)
        assertEquals(original.userId, restored.userId)
        assertEquals(original.speaking, restored.speaking)
    }

    @Test
    fun `PttToggle roundtrip`() {
        val original = PttToggle(active = true)
        val bytes = ProtocolSerializer.serializePttToggle(original)
        val restored = ProtocolSerializer.deserializePttToggle(bytes)
        assertTrue(restored.active)
    }

    @Test
    fun `UserMuteToggle roundtrip`() {
        val original = UserMuteToggle(muted = true)
        val bytes = ProtocolSerializer.serializeUserMuteToggle(original)
        val restored = ProtocolSerializer.deserializeUserMuteToggle(bytes)
        assertTrue(restored.muted)
    }

    // ================================================================
    // Key Rotation
    // ================================================================

    @Test
    fun `KeyRotationRequest roundtrip`() {
        val newKey = ByteArray(32) { (it + 1).toByte() }
        val encryptedKey = ByteArray(64)
        val original = KeyRotationRequest(
            newServerPublicKey = newKey,
            keyEpoch = 42,
            encryptedSessionKey = encryptedKey
        )
        val bytes = ProtocolSerializer.serializeKeyRotationRequest(original)
        val restored = ProtocolSerializer.deserializeKeyRotationRequest(bytes)
        assertArrayEquals(original.newServerPublicKey, restored.newServerPublicKey)
        assertEquals(original.keyEpoch, restored.keyEpoch)
        assertArrayEquals(original.encryptedSessionKey, restored.encryptedSessionKey)
    }

    // ================================================================
    // Control Message Wrapper
    // ================================================================

    @Test
    fun `Control message wrapper roundtrip for all types`() {
        val testCases = listOf(
            MessageType.LOGIN_REQUEST to LoginRequest(username = "test", authCredential = "pwd".toByteArray(), keyExchangeMethods = listOf("X25519"), clientPublicKey = ByteArray(32), clientUdpPort = 0, clientVideoUdpPort = 0),
            MessageType.JOIN_CHANNEL_REQUEST to JoinChannelRequest(channelId = 1L),
            MessageType.CHAT_SEND_REQUEST to ChatSendRequest(channelId = 1, text = "Hello"),
            MessageType.PTT_TOGGLE to PttToggle(active = true),
            MessageType.USER_MUTE_TOGGLE to UserMuteToggle(muted = false),
            MessageType.SPEAKING_STATE to SpeakingState(speaking = true),
            MessageType.ADMIN_AUTH_REQUEST to AdminAuthRequest(password = "admin"),
        )

        for ((msgType, msg) in testCases) {
            val bytes = ProtocolSerializer.serializeControlMessage(msgType, msg)
            val (restoredType, restoredMsg) = ProtocolSerializer.deserializeControlMessage(bytes)
            assertEquals(msgType, restoredType, "MessageType mismatch for $msgType")
            assertNotNull(restoredMsg, "Deserialized message is null for $msgType")
        }
    }

    // ================================================================
    // Edge Cases
    // ================================================================

    @Test
    fun `Empty string serialization`() {
        val original = ServerMessage(text = "")
        val bytes = ProtocolSerializer.serializeServerMessage(original)
        val restored = ProtocolSerializer.deserializeServerMessage(bytes)
        assertEquals("", restored.text)
    }

    @Test
    fun `Long string serialization`() {
        val longText = "A".repeat(10000)
        val original = ChatSendRequest(channelId = 1, text = longText)
        val bytes = ProtocolSerializer.serializeChatSendRequest(original)
        val restored = ProtocolSerializer.deserializeChatSendRequest(bytes)
        assertEquals(longText, restored.text)
    }

    @Test
    fun `Unicode string serialization`() {
        val unicodeText = "こんにちは世界！🎮🎯✨ 안녕하세요 세계! Привет мир!"
        val original = ServerMessage(text = unicodeText)
        val bytes = ProtocolSerializer.serializeServerMessage(original)
        val restored = ProtocolSerializer.deserializeServerMessage(bytes)
        assertEquals(unicodeText, restored.text)
    }

    @Test
    fun `File messages roundtrip`() {
        val entry = FileEntry(id = 1, channelId = 1, uploaderId = 42, filename = "test.pdf", fileSize = 1024, uploadTime = 1234567890)
        val listResponse = FileListResponse(entries = listOf(entry))
        val bytes = ProtocolSerializer.serializeFileListResponse(listResponse)
        val restored = ProtocolSerializer.deserializeFileListResponse(bytes)
        assertEquals(1, restored.entries.size)
        assertEquals("test.pdf", restored.entries[0].filename)
    }
}
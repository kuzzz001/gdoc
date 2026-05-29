import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Friend, FriendRequest, CollabInvitation, Message } from '@/types'
import { socialApi, messageApi, invitationApi } from '@/api/social'

export const useSocialStore = defineStore('social', () => {
  const friends = ref<Friend[]>([])
  const friendRequests = ref<FriendRequest[]>([])
  const invitations = ref<CollabInvitation[]>([])
  const messages = ref<Map<number, Message[]>>(new Map())
  const unreadTotal = ref(0)
  const activeChatUserId = ref<number | null>(null)
  const loading = ref(false)

  async function fetchFriends() {
    friends.value = await socialApi.getFriends()
  }

  async function fetchFriendRequests() {
    friendRequests.value = await socialApi.getPendingRequests()
  }

  async function fetchInvitations() {
    invitations.value = await invitationApi.list()
  }

  async function fetchMessages(friendId: number) {
    const msgs = await messageApi.getHistory(friendId)
    messages.value.set(friendId, msgs)
  }

  async function fetchUnreadCount() {
    unreadTotal.value = await messageApi.getUnreadCount()
  }

  async function markRead(friendId: number) {
    await messageApi.markRead(friendId)
    const friend = friends.value.find((f) => f.userId === friendId)
    if (friend) friend.unreadCount = 0
    fetchUnreadCount()
  }

  async function sendMessage(friendId: number, content: string) {
    const msg = await messageApi.send(friendId, content)
    const list = messages.value.get(friendId) || []
    list.push(msg)
    messages.value = new Map(messages.value.set(friendId, list))
  }

  function addMessage(msg: Message) {
    const friendId = msg.senderId === activeChatUserId.value ? msg.receiverId : msg.senderId
    const list = messages.value.get(friendId) || []
    list.push(msg)
    messages.value.set(friendId, list)
  }

  return {
    friends, friendRequests, invitations, messages, unreadTotal, activeChatUserId, loading,
    fetchFriends, fetchFriendRequests, fetchInvitations, fetchMessages, fetchUnreadCount,
    markRead, addMessage, sendMessage,
  }
})

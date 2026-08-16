package com.johnny.wekit.theme.util

import com.johnny.wekit.theme.data.ImageSlot

/**
 * Defines the complete image slot tree for a WeKit theme package.
 */
object ImageSlotTree {

    val ALL_SLOTS: List<ImageSlot> = buildList {
        // splash
        add(ImageSlot("splash/background.png", "background", "splash"))

        // home - top level
        add(ImageSlot("home/background.png", "background", "home"))
        add(ImageSlot("home/bottom_tab_background.png", "bottom_tab_background", "home"))
        add(ImageSlot("home/conversation_background.png", "conversation_background", "home"))
        add(ImageSlot("home/conversation_item_background.png", "conversation_item_background", "home"))
        add(ImageSlot("home/conversation_item_unread_badge.png", "conversation_item_unread_badge", "home"))
        add(ImageSlot("home/conversation_top_item_background.png", "conversation_top_item_background", "home"))
        add(ImageSlot("home/plus_menu_background.png", "plus_menu_background", "home"))
        add(ImageSlot("home/tab_contact_background.png", "tab_contact_background", "home"))
        add(ImageSlot("home/tab_discovery_background.png", "tab_discovery_background", "home"))
        add(ImageSlot("home/tab_me_background.png", "tab_me_background", "home"))

        // home/actionbar
        add(ImageSlot("home/actionbar/background.png", "background", "home"))
        add(ImageSlot("home/actionbar/multitask.png", "multitask", "home"))
        add(ImageSlot("home/actionbar/plus.png", "plus", "home"))
        add(ImageSlot("home/actionbar/search.png", "search", "home"))

        // home/tabs
        add(ImageSlot("home/tabs/contact_selected.png", "contact_selected", "home"))
        add(ImageSlot("home/tabs/contact_unselected.png", "contact_unselected", "home"))
        add(ImageSlot("home/tabs/conversation_selected.png", "conversation_selected", "home"))
        add(ImageSlot("home/tabs/conversation_unselected.png", "conversation_unselected", "home"))
        add(ImageSlot("home/tabs/discovery_selected.png", "discovery_selected", "home"))
        add(ImageSlot("home/tabs/discovery_unselected.png", "discovery_unselected", "home"))
        add(ImageSlot("home/tabs/me_selected.png", "me_selected", "home"))
        add(ImageSlot("home/tabs/me_unselected.png", "me_unselected", "home"))
        add(ImageSlot("home/tabs/unread_badge.png", "unread_badge", "home"))

        // home/items
        add(ImageSlot("home/items/group_chat.png", "group_chat", "home"))
        add(ImageSlot("home/items/label.png", "label", "home"))
        add(ImageSlot("home/items/new_friend.png", "new_friend", "home"))
        add(ImageSlot("home/items/official_account.png", "official_account", "home"))
        add(ImageSlot("home/items/only_chat_friends.png", "only_chat_friends", "home"))
        add(ImageSlot("home/items/service_account.png", "service_account", "home"))
        add(ImageSlot("home/items/wework_contact.png", "wework_contact", "home"))

        // home/discovery
        add(ImageSlot("home/discovery/channels.png", "channels", "home"))
        add(ImageSlot("home/discovery/games.png", "games", "home"))
        add(ImageSlot("home/discovery/listen.png", "listen", "home"))
        add(ImageSlot("home/discovery/live.png", "live", "home"))
        add(ImageSlot("home/discovery/look.png", "look", "home"))
        add(ImageSlot("home/discovery/mini_programs.png", "mini_programs", "home"))
        add(ImageSlot("home/discovery/moments.png", "moments", "home"))
        add(ImageSlot("home/discovery/nearby.png", "nearby", "home"))
        add(ImageSlot("home/discovery/scan.png", "scan", "home"))
        add(ImageSlot("home/discovery/search.png", "search", "home"))
        add(ImageSlot("home/discovery/shopping.png", "shopping", "home"))

        // home/me
        add(ImageSlot("home/me/cards.png", "cards", "home"))
        add(ImageSlot("home/me/emoji.png", "emoji", "home"))
        add(ImageSlot("home/me/favorites.png", "favorites", "home"))
        add(ImageSlot("home/me/moments.png", "moments", "home"))
        add(ImageSlot("home/me/services.png", "services", "home"))
        add(ImageSlot("home/me/settings.png", "settings", "home"))

        // chat - top level
        add(ImageSlot("chat/input_background.png", "input_background", "chat"))
        add(ImageSlot("chat/plus_panel_background.png", "plus_panel_background", "chat"))
        add(ImageSlot("chat/red_packet.png", "red_packet", "chat"))
        add(ImageSlot("chat/red_packet_background.png", "red_packet_background", "chat"))
        add(ImageSlot("chat/red_packet_open.png", "red_packet_open", "chat"))
        add(ImageSlot("chat/speech_speed.png", "speech_speed", "chat"))
        add(ImageSlot("chat/public_account_switch.png", "public_account_switch", "chat"))
        add(ImageSlot("chat/history_tongue_arrow_down.png", "history_tongue_arrow_down", "chat"))
        add(ImageSlot("chat/history_tongue_arrow_up.png", "history_tongue_arrow_up", "chat"))
        add(ImageSlot("chat/history_tongue_background.png", "history_tongue_background", "chat"))
        add(ImageSlot("chat/long_press_menu_arrow_down.png", "long_press_menu_arrow_down", "chat"))
        add(ImageSlot("chat/long_press_menu_arrow_up.png", "long_press_menu_arrow_up", "chat"))
        add(ImageSlot("chat/long_press_menu_background.png", "long_press_menu_background", "chat"))

        // chat/actionbar
        add(ImageSlot("chat/actionbar/background.png", "background", "chat"))
        add(ImageSlot("chat/actionbar/back.png", "back", "chat"))
        add(ImageSlot("chat/actionbar/more.png", "more", "chat"))

        // chat/bubbles
        add(ImageSlot("chat/bubbles/text_left.png", "text_left", "chat"))
        add(ImageSlot("chat/bubbles/text_right.png", "text_right", "chat"))
        add(ImageSlot("chat/bubbles/file_left.png", "file_left", "chat"))
        add(ImageSlot("chat/bubbles/file_right.png", "file_right", "chat"))
        add(ImageSlot("chat/bubbles/red_packet_left.png", "red_packet_left", "chat"))
        add(ImageSlot("chat/bubbles/red_packet_right.png", "red_packet_right", "chat"))
        add(ImageSlot("chat/bubbles/transfer_left.png", "transfer_left", "chat"))
        add(ImageSlot("chat/bubbles/transfer_right.png", "transfer_right", "chat"))

        // chat/emoji_tabs
        add(ImageSlot("chat/emoji_tabs/custom.png", "custom", "chat"))
        add(ImageSlot("chat/emoji_tabs/favorites.png", "favorites", "chat"))
        add(ImageSlot("chat/emoji_tabs/search.png", "search", "chat"))
        add(ImageSlot("chat/emoji_tabs/system.png", "system", "chat"))

        // plus
        add(ImageSlot("plus/album.png", "album", "plus"))
        add(ImageSlot("plus/business_card.png", "business_card", "plus"))
        add(ImageSlot("plus/camera.png", "camera", "plus"))
        add(ImageSlot("plus/card.png", "card", "plus"))
        add(ImageSlot("plus/chain.png", "chain", "plus"))
        add(ImageSlot("plus/favorites.png", "favorites", "plus"))
        add(ImageSlot("plus/file.png", "file", "plus"))
        add(ImageSlot("plus/gift.png", "gift", "plus"))
        add(ImageSlot("plus/group_tools.png", "group_tools", "plus"))
        add(ImageSlot("plus/live.png", "live", "plus"))
        add(ImageSlot("plus/location.png", "location", "plus"))
        add(ImageSlot("plus/music.png", "music", "plus"))
        add(ImageSlot("plus/red_packet.png", "red_packet", "plus"))
        add(ImageSlot("plus/transfer.png", "transfer", "plus"))
        add(ImageSlot("plus/video_call.png", "video_call", "plus"))
        add(ImageSlot("plus/voice_input.png", "voice_input", "plus"))

        // settings
        add(ImageSlot("settings/background.png", "background", "settings"))
        add(ImageSlot("settings/actionbar/back.png", "back", "settings"))
        add(ImageSlot("settings/actionbar/background.png", "background", "settings"))
        add(ImageSlot("settings/actionbar/more.png", "more", "settings"))
        add(ImageSlot("settings/actionbar/search.png", "search", "settings"))
    }

    /** Group slots by category */
    fun groupByCategory(): Map<String, List<ImageSlot>> {
        return ALL_SLOTS.groupBy { it.category }
    }
}

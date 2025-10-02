/**
 * G.F.B.S. Mirage (mirage_gfbs) - A Minecraft Mod
 * Copyright (C) 2025-2029 Convex89524

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.mirage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Mirage_gfbs.MODID);

    public static final Map<String, RegistryObject<SoundEvent>> SOUND_EVENTS_MAP = new HashMap<>();

    static {
        // 警报
        registerToMap("alarm.c7_noboom_a");
        registerToMap("alarm.emergency_a");
        registerToMap("alarm.meltdown_a");
        registerToMap("alarm.overheat_a");
        registerToMap("alarm.overpressure_a");
        registerToMap("alarm.war_a");

        // 爆炸
        registerToMap("boom.boom2_b");
        registerToMap("boom.boom3_b");
        registerToMap("boom.boom4_b");
        registerToMap("boom.boom_5_what_b");
        registerToMap("boom.boom_6_what_b");
        registerToMap("boom.boom_7_what_b");
        registerToMap("boom.boom_b");
        registerToMap("boom.dmr_b");

        // F.A.A.S.
        registerToMap("faas.dmr_e_s_s");
        registerToMap("faas.dmr_e_s_t");
        registerToMap("faas.dmr_f_e");
        registerToMap("faas.dmr_h_s_d_p_r");
        registerToMap("faas.dmr_i_c");
        registerToMap("faas.dmr_i_c_f");
        registerToMap("faas.dmr_i_c_f_b");
        registerToMap("faas.dmr_l_c");
        registerToMap("faas.dmr_m_m");
        registerToMap("faas.dmr_o");
        registerToMap("faas.dmr_s_e_s_i_d");
        registerToMap("faas.dmr_s_m_r_i_s_l");
        registerToMap("faas.dmr_start_1");
        registerToMap("faas.dmr_start_2");
        registerToMap("faas.dmr_w_s_i_f_m");
        registerToMap("faas.dmr_w_s_i_t_m");
        registerToMap("faas.efss_start");
        registerToMap("faas.f_b_c_r_t");
        registerToMap("faas.f_e_p_o_n");
        registerToMap("faas.f_e_w");
        registerToMap("faas.f_l_b_a");
        registerToMap("faas.f_m_c_s_o");
        registerToMap("faas.f_r_s");
        registerToMap("faas.faas_a_p");
        registerToMap("faas.faas_ahhh");
        registerToMap("faas.m_s_f");
        registerToMap("faas.startup_button_nolock_open");

        // 广播
        registerToMap("human.shift_h");

        // 音乐
        registerToMap("music.adminroom_chair_duang");
        registerToMap("music.diagonal_ele_1_m");
        registerToMap("music.diagonal_ele_2_m");
        registerToMap("music.dmr_rare_startup_m");
        registerToMap("music.dmr_startup_m");
        registerToMap("music.dududududu");
        registerToMap("music.ele_1_m");
        registerToMap("music.ele_2_m");
        registerToMap("music.new_p1_m");
        registerToMap("music.new_p2_m");
        registerToMap("music.pi_ok_m");
        registerToMap("music.qs_open_m");
        registerToMap("music.scpsl_end");
        registerToMap("music.scpsl_end_oboe");
        registerToMap("music.unknown2_m");
        registerToMap("music.unknown3_m");
        registerToMap("music.unknown4_m");
        registerToMap("music.unknown5_m");
        registerToMap("music.unknown_m");
        registerToMap("music.wuhu");

        // 环境
        registerToMap("surroundings.airlock");
        registerToMap("surroundings.alpha_pumo_startup2");
        registerToMap("surroundings.alpha_pump_closeing");
        registerToMap("surroundings.alpha_pump_start3");
        registerToMap("surroundings.alpha_pump_startup");
        registerToMap("surroundings.bigdoor_close");
        registerToMap("surroundings.bigdoor_open");
        registerToMap("surroundings.bigdoor_open2");
        registerToMap("surroundings.collider");
        registerToMap("surroundings.collider2");
        registerToMap("surroundings.ele_door_close");
        registerToMap("surroundings.electrocuted");
        registerToMap("surroundings.fan_off");
        registerToMap("surroundings.fan_startup");
        registerToMap("surroundings.pgr_1");
        registerToMap("surroundings.pgr_2");
        registerToMap("surroundings.reactor");
        registerToMap("surroundings.water");

        // 弹头
        registerToMap("war.war");
        registerToMap("war.war_yes");
        registerToMap("war.main90");
    }

    private static void registerToMap(String name) {
        SOUND_EVENTS_MAP.put(name, SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(Mirage_gfbs.MODID, name))));
    }

    public static RegistryObject<SoundEvent> getSoundEvent(String name) {
        return SOUND_EVENTS_MAP.get(name);
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
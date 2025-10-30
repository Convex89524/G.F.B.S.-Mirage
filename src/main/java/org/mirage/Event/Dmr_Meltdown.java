package org.mirage.Event;

import org.mirage.Command.MirageGFBsEventCommand;
import org.mirage.Tools.Task;

import java.util.concurrent.TimeUnit;

import static org.mirage.CommandExecutor.executeCommand;

public class Dmr_Meltdown {
    public static void execute(MirageGFBsEventCommand.CommandContext context) {
        NeiBao();

        Task.delay(()->{
            executeCommand("playsound mirage_gfbs:music.new_p1_m voice @a ~ ~ ~ 1 1 1");

            Task.delay(()->{
                executeCommand("Notification @a 200 F.A.A.S. F.A.A.S.中央控制节点无法访问 至 DMR控制节点, 重新构建协议...");
            }, 10000, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("Notification @a 200 F.A.A.S. F.A.M.S.软件出现多处异常, DMR控制节点失效.");
            }, 15000, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:human.emergency.c_r_p_e voice @a ~ ~ ~ 1 1 1");
                executeCommand("Notification @a 200 Deputy.Reactor.Supervisor. 红色紧急情况代码已发布, 请立即疏散.");
            }, 30000, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:human.dmr.s_t_b_e_r_a voice @a ~ ~ ~ 1 1 1");
                executeCommand("Notification @a 300 Safety.Supervisor. 所有设施人员注意, 在DMR关闭之前一个人不准跑, 否则直接枪毙, 这是你唯一的警告.");
            }, 142541, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                NeiBao();
            }, 137616, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("Notification @a 200 F.A.A.S. 正在计算融毁时间...");
                executeCommand("playsound mirage_gfbs:faas_s._f_s_286753 voice @a ~ ~ ~ 1.2 1 1");

                executeCommand("playsound mirage_gfbs:surroundings.pgr_2 voice @a ~ ~ ~ 1.2 1 1");
                executeCommand("CameraShake @a 16 0.05 4800 490 3290");
            }, 165412, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:faas.dmr_w_s_i_t_m voice @a ~ ~ ~ 0.9 1 1");
                executeCommand("Notification @a 200 F.A.A.S. 危险, DMR融毁在倒计时-10分钟, 关机窗口结束时间为倒计时-5分钟.");
            }, 176508, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:faas.dmr_s_e_s_i_d voice @a ~ ~ ~ 0.9 1 1");
                executeCommand("Notification @a 300 F.A.A.S. 紧急关机窗口已开启, 温度必须低于3000k.");
            }, 190810, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:human.work.f_s_d_r_a_c record @a ~ ~ ~ 10000000 1 1");
                executeCommand("Notification @a 300 Helen.Kate. 操作员们, F.A.A.S.工程师们正在尝试修复服务器残骸, 以破解关机代码. 留意你们这边的情况.");
            }, 226134, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                NeiBao();
                executeCommand("playsound mirage_gfbs:faas.faas_a_p voice @a ~ ~ ~ 0.9 1 1");
            }, 272000, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                NeiBao();
                executeCommand("playsound mirage_gfbs:faas.faas_a_p voice @a ~ ~ ~ 0.9 1 1");
            }, 337993, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:faas.dmr_w_s_i_f_m voice @a ~ ~ ~ 1 1 1");
                executeCommand("Notification @a 200 F.A.A.S. 危险, DMR预计将在倒计时-5分钟后爆炸, 反应堆关机选项现已失效.");
            }, 347000, TimeUnit.MILLISECONDS);

            Task.delay(()->{
                executeCommand("playsound mirage_gfbs:faas.f_b_c_r_t voice @a ~ ~ ~ 1 1 1");
                executeCommand("Notification @a 200 F.A.A.S. 全体设施人员注意, 已发布黑色紧急指令, 请立即撤离至塔塔鲁斯上层区域.");
                executeCommand("playsound mirage_gfbs:music.new_p2_m voice @a ~ ~ ~ 1 1 1");

                Task.delay(()->{
                    executeCommand("playsound mirage_gfbs:faas.f_m_c_s_o voice @a ~ ~ ~ 1 1 1");
                    executeCommand("Notification @a 200 F.A.A.S. 控制系统出现错误, 反应堆控制系统对暗物质反应堆无响应, 处于主控制节点失效状态.");

                    Task.delay(()->{
                        executeCommand("playsound mirage_gfbs:human.dmr.p2 voice @a ~ ~ ~ 1 1 1");
                        executeCommand("Notification @a 200 Facilities.Supervisor. 所有反应堆操作小组人员注意, 这是我们阻止DMR彻底破坏的最后机会了, 爬到上层结构, 在1到3秒的时间内依次将所有燃料电池弹出, 以引发燃烧性熄火故障并关闭暗物质反应堆, 你还有1分钟的时间, 祝你好运.");
                    }, 9673 , TimeUnit.MILLISECONDS);
                }, 20523, TimeUnit.MILLISECONDS);

                Task.delay(()->{
                    executeCommand("playsound mirage_gfbs:faas.dmr_o voice @a ~ ~ ~ 1 1 1");
                    executeCommand("Notification @a 200 F.A.A.S. 暗物质压力清除系统不起作用, 压力持续上升, 上层结构完整性可能进一步遭受损坏.");
                }, 54436, TimeUnit.MILLISECONDS);

                Task.delay(()->{
                    executeCommand("playsound mirage_gfbs:faas.m_s_f voice @a ~ ~ ~ 1 1 1");
                    executeCommand("Notification @a 200 F.A.A.S. 监控系统故障, 无法预测反应堆爆炸, 代码Omni紧急状态现已发行, 封锁措施将在倒计时-2分钟后实施.");
                }, 66180, TimeUnit.MILLISECONDS);

                Task.delay(()->{
                    executeCommand("playsound mirage_gfbs:faas.f_e_p_o_n voice @a ~ ~ ~ 1 1 1");
                    executeCommand("Notification @a 200 F.A.A.S. 所有紧急工作装置全部依赖于紧急发电机, 现已降低整体耗电功率.");
                }, 82972, TimeUnit.MILLISECONDS);

                Task.delay(()->{
                    executeCommand("playsound mirage_gfbs:faas.f_l_b_a voice @a ~ ~ ~ 1 1 1");
                    executeCommand("Notification @a 200 F.A.A.S. 注意, 封锁措施现已启动, 防爆门将在一分钟后关闭.");
                }, 142786, TimeUnit.MILLISECONDS);

                Task.delay(()->{
                    executeCommand("CameraShake @a 30 0.1 21800 290 10290");
                    executeCommand("playsound mirage_gfbs:boom.dmr_b voice @a ~ ~ ~ 2 1 1");
                }, 196454, TimeUnit.MILLISECONDS);

            }, 360000, TimeUnit.MILLISECONDS);

        }, 5000, TimeUnit.MILLISECONDS);

        Task.delay(()->{

        }, 27500, TimeUnit.MILLISECONDS);
    }

    private static void NeiBao(){
        executeCommand("playsound mirage_gfbs:boom.boom_7_what_b voice @a ~ ~ ~ 1 1 1");
        executeCommand("CameraShake @a 15 0.1 1800 290 1290");
        Task.delay(()->{
            executeCommand("CameraShake @a 15 0.1 4800 490 3290");
        },500, TimeUnit.MILLISECONDS);
    }
}

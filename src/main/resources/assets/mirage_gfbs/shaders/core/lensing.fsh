#version 150

// 输入输出
in vec2 texCoord;
out vec4 fragColor;

// Uniforms
uniform mat4 ProjMat;
uniform mat4 ViewMat;
uniform mat4 InvProjMat;
uniform mat4 InvViewMat;
uniform vec3 CameraPos;
uniform vec2 ScreenSize;
uniform sampler2D DiffuseSampler;   // 颜色纹理 (单元0)
uniform sampler2D DepthSampler;    // 深度纹理 (单元1)
uniform sampler2D EventHorizonTexture; // 事件视界纹理 (单元2)
uniform int BlackHoleCount;
uniform vec3 BlackHolePos[8];
uniform float LensingFactor[8];
uniform float EventHorizon[8];
uniform float Alpha[8];

// 计算世界坐标的函数
vec3 calculateWorldPosition(vec2 uv, float depth) {
    // 将UV坐标转换为NDC坐标 [-1,1]
    vec4 clipSpace = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    // 转换到视图空间
    vec4 viewSpace = InvProjMat * clipSpace;
    viewSpace /= viewSpace.w;

    // 转换到世界空间
    vec4 worldSpace = InvViewMat * viewSpace;
    return worldSpace.xyz;
}

// 计算单个黑洞的引力透镜偏移
vec3 calculateBlackHoleOffset(int index, vec3 worldPos) {
    // 计算到黑洞的向量
    vec3 toBlackHole = BlackHolePos[index] - worldPos;
    float dist = length(toBlackHole);

    // 避免除以零
    if (dist < 0.001) {
        return vec3(0.0);
    }

    vec3 dir = toBlackHole / dist;

    // 计算引力透镜效应强度
    float lensingStrength = LensingFactor[index] / dist;

    return lensingStrength * dir;
}

// 计算引力透镜效应
vec3 applyLensingEffect(vec3 worldPos, vec3 rayDir, vec3 color) {
    // 累积所有黑洞的效应
    vec3 finalColor = color;
    vec3 accumulatedOffset = vec3(0.0);

    // 修复：使用固定循环上限，并在内部判断break
    for (int i = 0; i < 8; i++) {
        if (i >= BlackHoleCount) {
            break;
        }
        accumulatedOffset += calculateBlackHoleOffset(i, worldPos);
    }

    // 应用总偏移
    if (length(accumulatedOffset) > 0.0) {
        vec3 bentRay = normalize(rayDir + accumulatedOffset);

        // 计算新光线方向的世界坐标
        vec3 newWorldPos = worldPos + bentRay * 0.1;

        // 将新位置投影回屏幕空间
        vec4 viewPos = ViewMat * vec4(newWorldPos, 1.0);
        vec4 projPos = ProjMat * viewPos;
        projPos /= projPos.w;
        vec2 newUV = projPos.xy * 0.5 + 0.5;

        // 使用clamp确保UV在有效范围内，而不是使用条件判断
        newUV = clamp(newUV, 0.0, 1.0);

        // 采样新位置的颜色
        finalColor = texture(DiffuseSampler, newUV).rgb;
    }

    return finalColor;
}

// 渲染事件视界
vec4 renderEventHorizon(vec3 worldPos, vec3 color) {
    vec4 result = vec4(color, 1.0);

    for (int i = 0; i < 8; i++) {
        if (i >= BlackHoleCount) {
            break;
        }

        // 计算到黑洞的距离
        float dist = distance(worldPos, BlackHolePos[i]);

        // 检查是否在事件视界内
        if (dist < EventHorizon[i]) {
            // 计算事件视界UV
            vec3 toBlackHole = normalize(BlackHolePos[i] - worldPos);
            vec2 uv = vec2(atan(toBlackHole.z, toBlackHole.x), acos(toBlackHole.y));
            uv = uv / vec2(3.1415926535, 3.1415926535) * 0.5 + 0.5;

            // 采样事件视界纹理
            vec4 horizonColor = texture(EventHorizonTexture, uv);
            horizonColor.a *= Alpha[i]; // 应用透明度

            // 混合颜色
            result = mix(result, horizonColor, horizonColor.a);

            // 找到一个黑洞后就可以返回，因为一个点不可能同时在多个事件视界内
            return result;
        }
    }

    return result;
}

void main() {
    // 获取深度值
    float depth = texture(DepthSampler, texCoord).r;

    // 计算世界坐标
    vec3 worldPos = calculateWorldPosition(texCoord, depth);

    // 计算从相机到像素的射线方向
    vec3 rayDir = normalize(worldPos - CameraPos);

    // 采样原始颜色
    vec3 originalColor = texture(DiffuseSampler, texCoord).rgb;

    // 应用引力透镜效应
    vec3 lensedColor = applyLensingEffect(worldPos, rayDir, originalColor);

    // 渲染事件视界
    fragColor = renderEventHorizon(worldPos, lensedColor);

    vec2 offset = vec2(0.005, 0.005);
    vec3 chroma = vec3(
    texture(DiffuseSampler, texCoord + offset).r,
    originalColor.g,
    texture(DiffuseSampler, texCoord - offset).b
    );

    // 混合色差效果
    fragColor.rgb = mix(fragColor.rgb, chroma, 0.1);
}
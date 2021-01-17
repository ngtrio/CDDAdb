# list all
列出该类型下的所有项
- [x] `GET /{type}
```json
[
  {
      "id":"mon_hallu_mom", // json的唯一标识
      "name":"你妈",		  // 名字
      "symbol":"@",         // 符号
      "color":["white",""]  // 1: 符号的字体颜色 2: 符号的背景颜色
  }
]
```

# Monster
获取id对应的完整json
- [x] `GET /monster/{id}`
```json
{
  "armor_cut": 4,         // 斩击防护
  "armor_bash": 6,        // 钝击防护
  "armor_bullet": 3,      // 子弹防护
  "armor_stab": 0,        // 刺击防护
  "armor_acid": 0,        // 酸性防护
  "armor_fire": 0,        // 火焰防护
  "melee_skill": 6,       // 近战技能
  // 下面三个计算伤害
  // 格式如 4d6+2，d和+是固定字符，数字对应下面三项
  "melee_dice": 4,        
  "melee_dice_sides": 6,
  "melee_cut": 2,

  "dodge": 0,             // 闪避
  "diff": 0,
  "special_attacks": [    // 特殊攻击
    [
      "FLESH_GOLEM",      // 名称
      8                   // 冷却
    ],
    [
      "ABSORB_MEAT",
      1
    ]
  ],
  "emit_field": [],
  "attack_cost": 100,
  "morale": 100,          // 士气
  "aggression": 100,      // 侵略性
  "vision_day": 50,       // 视力（昼）
  "vision_night": 3,      // 视力（夜）
  "symbol": "j",          // 怪物的符号
  "color": [              // 符号的颜色，两个字段分别为字体颜色和背景颜色
    "darkgray",
    "red"
  ],
  "hp": 200,              // 血量
  "flags": [
    "SEES",
    "HEARS",
    "SMELLS",
    "STUMBLES",
    "WARM",
    "ATTACKMON",
    "POISON"
  ],
  // 描述
  "description": "一只体型高耸的怪诞人型怪物，由痉挛的肌肉和器官融合而成。各种器官从它笨重的身体上脱落，但马上就重新被其躯体吸收。",
  "type": "MONSTER",
  "speed": 80,                        // 速度
  "id": "mon_flesh_golem",          
  "weight": "120 kg",                 // 体重
  "volume": "300.0L(较大)",            // 体型
  "name": "血肉傀儡",                   // 名字
  "difficulty": "29.38880(高威胁。)"    // 难度
}
```

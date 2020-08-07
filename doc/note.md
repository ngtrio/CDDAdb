# 数据文件下载
* [po file](https://www.transifex.com/cataclysm-dda-translators/cataclysm-dda/master-cataclysm-dda/zh_CN/download/for_use/)

# JSON格式
[官方文档](https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INFO.md)
# color格式
[官方文档](https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/COLOR.md)

# Monster

## difficulty

### level

```
if( type->difficulty < 3 ) {
            difficulty_str = _( "<color_light_gray>Minimal threat.</color>" );
        } else if( type->difficulty < 10 ) {
            difficulty_str = _( "<color_light_gray>Mildly dangerous.</color>" );
        } else if( type->difficulty < 20 ) {
            difficulty_str = _( "<color_light_red>Dangerous.</color>" );
        } else if( type->difficulty < 30 ) {
            difficulty_str = _( "<color_red>Very dangerous.</color>" );
        } else if( type->difficulty < 50 ) {
            difficulty_str = _( "<color_red>Extremely dangerous.</color>" );
        } else {
            difficulty_str = _( "<color_red>Fatally dangerous!</color>" );
        }
```
### calculate

```
difficulty = ( melee_skill + 1 ) * melee_dice * ( bonus_cut + melee_sides ) * 0.04 +
                 ( sk_dodge + 1 ) * ( 3 + armor_bash + armor_cut ) * 0.04 +
                 ( difficulty_base + special_attacks.size() + 8 * emit_fields.size() );
    difficulty *= ( hp + speed - attack_cost + ( morale + agro ) * 0.1 ) * 0.01 +
                  ( vision_day + 2 * vision_night ) * 0.01;
```

## volume to size

```
static creature_size volume_to_size( const units::volume &vol )
{
    if( vol <= 7500_ml ) {
        return creature_size::tiny;
    } else if( vol <= 46250_ml ) {
        return creature_size::small;
    } else if( vol <= 77500_ml ) {
        return creature_size::medium;
    } else if( vol <= 483750_ml ) {
        return creature_size::large;
    }
    return creature_size::huge;
}
```
# Armor
## 防护
```
int item::bash_resist( bool to_self ) const
{

    // bash cut bullet
    // 全新时，eff_damage为0
    const int eff_thickness = std::max( 1, get_thickness() - eff_damage );

    const std::vector<const material_type *> mat_types = made_of_types();
    if( !mat_types.empty() ) {
        for( const material_type *mat : mat_types ) {
            resist += mat->bash_resist();
        }
        // Average based on number of materials.
        resist /= mat_types.size();
    }

    // 不考虑强化，mod为0
    return std::lround( ( resist * eff_thickness ) + mod );

    // fire, acid
    if( !mat_types.empty() ) {
            // Not sure why cut and bash get an armor thickness bonus but acid doesn't,
            // but such is the way of the code.
    
            for( const material_type *mat : mat_types ) {
                resist += mat->acid_resist();
            }
            // Average based on number of materials.
            resist /= mat_types.size();
        }
    
        const int env = get_env_resist( base_env_resist );
        if( env < 10 ) {
            // Low env protection means it doesn't prevent acid seeping in.
            resist *= env / 10.0f;
        }
        return std::lround( resist + mod );
    }
}
```
## Melee
### attack time 
```
int ret = 65 + ( volume() / 62.5_ml + weight() / 60_gram ) / 1;
```
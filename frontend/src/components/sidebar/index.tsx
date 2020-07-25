import React from "react";
import {Menu} from "antd";

const {SubMenu} = Menu

class SideBar extends React.Component {
    render() {
        return (
            <Menu mode={"inline"}>
                <Menu.Item>
                    怪物
                </Menu.Item>
                <SubMenu title={"物品"}>
                    <Menu.Item>
                        书籍
                    </Menu.Item>
                </SubMenu>
            </Menu>
        )
    }
}

export default SideBar
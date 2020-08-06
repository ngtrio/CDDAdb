import React from "react";
import {Menu} from "antd";
import {Link} from "react-router-dom";

const {SubMenu} = Menu

class SideBar extends React.Component {
    render() {
        return (
            <Menu mode={"inline"}>
                <Menu.Item>
                    <Link to="/monster">怪物</Link>
                </Menu.Item>
                <SubMenu title={"物品"}>
                    <Menu.Item>
                        <Link to="/book">书籍 </Link>
                    </Menu.Item>
                </SubMenu>
            </Menu>
        )
    }
}

export default SideBar
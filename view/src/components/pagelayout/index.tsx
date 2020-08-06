import {Col, Row} from "antd";
import React from "react";
import "./index.css"
import SideBar from "../sidebar";
import InfoCard from "../infocard";
import Showcase from "../showcase";

class PageLayout extends React.Component {
    render() {
        return (
            <Row
                id="container"
                gutter={16}
                justify="center"
            >
                <Col span={3}><SideBar/></Col>
                <Col span={13}>
                    <InfoCard/>
                </Col>
                <Col span={4}>
                    <Showcase/>
                </Col>
            </Row>
        )
    }
}

export default PageLayout
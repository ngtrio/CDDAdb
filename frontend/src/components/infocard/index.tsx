import {Card, Divider} from "antd";
import React from "react";

const {Meta} = Card

export default function InfoCard() {
    return (
        <Card
            hoverable={true}
        >
            aaa
            bbb

            bbb
            <Divider/>
            <Meta
                title="Card title"
                description="This is the description=============================================="
            />
        </Card>
    )
}
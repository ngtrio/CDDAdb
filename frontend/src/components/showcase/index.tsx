import {Card, List, Typography} from "antd";
import React from "react";
import {request} from "../../utils/request";

interface Props {
    type: string
}

interface NameInfo {
    name: string,
    symbol: string,
    color: string[]
}

interface State {
    content: NameInfo[]
}

class Showcase extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            content: []
        }
    }

    componentWillMount() {
        let type = this.props.type
        let url = "http://localhost:9000/" + type
        request(url, "GET")
            .then(data => this.setState({
                content: data
            }))
            .catch(err => console.log(err))
    }

    render() {
        const {content} = this.state

        let renderItem = (item: NameInfo) => {
            return (
                <List.Item>
                    <Typography.Text>
                        <span style={{color: item.color[0], backgroundColor: item.color[1]}}>
                            {item.symbol}
                        </span> {item.name}
                    </Typography.Text>
                </List.Item>
            )
        }

        return (
            <Card hoverable={true}>
                <List
                    bordered
                    dataSource={content}
                    renderItem={item => renderItem(item)}
                />
            </Card>
        )
    }
}

export default Showcase
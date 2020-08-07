import {Card, List, Typography} from "antd";
import React from "react";
import {request} from "../../utils/request";
import {Link, RouteComponentProps, withRouter} from "react-router-dom";

interface Props extends RouteComponentProps<any> {
}

interface Info {
    [propName: string]: any
}

interface State {
    content: Info[]
}

class Showcase extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            content: []
        }
        this.fetchData()
    }

    componentDidUpdate(prevProps: Readonly<Props>) {
        let preType = prevProps.match.params.type
        let type = this.props.match.params.type
        if (preType === undefined || preType !== type) {
            this.fetchData()
        }
    }

    fetchData() {
        const {type} = this.props.match.params
        if (type !== undefined) {
            let url = `http://localhost:9000/${type}`
            request(url, "GET")
                .then(data => this.setState({
                    content: data
                }))
                .catch(err => console.log(err))
        }
    }

    render() {
        const {content} = this.state
        const {type} = this.props.match.params

        let renderItem = (item: Info) => {
            return (
                <Link to={"/" + type + "/" + item.id}>
                    <List.Item>
                        <Typography.Text>
                        <span style={{color: item.color[0], backgroundColor: item.color[1]}}>
                            {item.symbol}
                        </span> {item.name}
                        </Typography.Text>
                    </List.Item>
                </Link>
            )
        }
        return (
            <Card>
                <List
                    dataSource={content}
                    renderItem={item => renderItem(item)}
                />
            </Card>
        )
    }
}

export default withRouter(Showcase)
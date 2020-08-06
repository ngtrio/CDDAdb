import {Card, Divider, Empty, Spin} from "antd";
import React from "react";
import {request} from "../../utils/request";
import {RouteComponentProps, withRouter} from "react-router-dom";

const {Meta} = Card

interface Props extends RouteComponentProps<any> {
}

interface Info {
    [propName: string]: any
}

interface State {
    notFound: boolean,
    needed: boolean,
    loading: boolean
    content: Info
}

class InfoCard extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {
            notFound: false,
            needed: false,
            loading: true,
            content: {}
        }
        this.fetchData()
    }

    componentDidUpdate(preProps: Props) {
        if (preProps !== this.props) {
            this.setState({
                needed: false,
                loading: true
            })
            this.fetchData()
        }
    }

    fetchData() {
        const {type, name} = this.props.match.params
        if (name !== undefined) {
            let url = "http://120.24.60.156:9000/" + type + "/" + name
            request(url, 'GET')
                .then(data => {
                    this.setState({
                        notFound: data.name === undefined,
                        needed: true,
                        loading: false,
                        content: data
                    })
                })
                .catch(err => console.log(err))
        }
    }

    render() {
        const {content, needed, loading, notFound} = this.state
        if (!needed) return <Card><Empty/></Card>
        if (loading) {
            return <Card><Spin/></Card>
        } else {
            if (notFound) return <Card><Empty/></Card>
            let title = (
                <div>
                    <span
                        style={{
                            color: content.color[0],
                            backgroundColor: content.color[1]
                        }}>
                    {content.symbol}
                    </span> {content.name}
                </div>
            )
            return (
                <Card>
                    {JSON.stringify(content)}
                    <Divider/>
                    <Meta
                        title={title}
                        description={content.description}
                    />
                </Card>
            )
        }
    }
}

export default withRouter(InfoCard)
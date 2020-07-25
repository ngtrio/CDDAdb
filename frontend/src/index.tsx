import ReactDOM from 'react-dom';
import React from 'react';
import PageLayout from "./components/pagelayout";
import './index.css'

class Main extends React.Component {
    render() {
        return (
            <PageLayout/>
        );
    }
}

ReactDOM.render(<Main/>, document.getElementById("root"));
import ReactDOM from 'react-dom';
import React from 'react';
import PageLayout from "./components/pagelayout";
import './index.css'
import {BrowserRouter, Route, Switch} from "react-router-dom";

class Main extends React.Component {
    render() {
        return (
            <BrowserRouter>
                <Switch>
                    <Route path="/:type/:id" component={PageLayout}/>
                    <Route path="/:type" component={PageLayout}/>
                    <Route path="/" component={PageLayout}/>
                </Switch>
            </BrowserRouter>
        );
    }
}

ReactDOM.render(<Main/>, document.getElementById("root"));
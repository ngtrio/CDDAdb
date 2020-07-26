import ReactDOM from 'react-dom';
import React from 'react';
import PageLayout from "./components/pagelayout";
import './index.css'
import {BrowserRouter, Route, Switch} from "react-router-dom";
import NotFound from "./components/404";

class Main extends React.Component {
    render() {
        return (
            <BrowserRouter>
                <Switch>
                    <Route path="/:type/:name" component={PageLayout}/>
                    <Route path="/:type" component={PageLayout}/>
                    <Route path="/404" component={NotFound}/>
                </Switch>
            </BrowserRouter>
        );
    }
}

ReactDOM.render(<Main/>, document.getElementById("root"));
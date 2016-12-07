import React, {Component} from 'react'
import {Link} from 'react-router'
import ActionBinder from './../util/ActionBinder'
// Actions
import * as AddRegistryActions from './../actions/AddRegistryActions'
import * as AddRepoActions from './../actions/AddRepoActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		this.state = {
			registries: [],
			addRegistry: {
				...AddRegistryActions.addRegistryState(),
			},
			addRepo: {
				...AddRepoActions.addRepoState()
			}
		};
	}
	getChildContext(){
		return {
			actions: ActionBinder([AddRegistryActions, AddRepoActions], this),
			state: this.state
		};
	}
	componentDidMount() {
		
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
				 <div className="MaxWidthContainer">
					<h2>
						<Link to="/registries">Europa</Link>
					</h2>

					<div className="FlexRow NavButtonContainer">
						<div className="Flex1">
							<Link to="/repositories">
								<i className="icon icon-dis-package"/>
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/registries">
								<i className="icon icon-dis-docker" style={{fontSize: '1.4rem'}}/>
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/settings">
								<i className="icon icon-dis-settings"/>
							</Link>
						</div>
					</div>
					</div>
				</nav>
				<div className="PageContent">
					<div className="MaxWidthContainer">
						{this.props.children}
					</div>
				</div>
			</div>
		);
	}
}

Layout.childContextTypes = {
	actions: React.PropTypes.object,
	state: React.PropTypes.object
};


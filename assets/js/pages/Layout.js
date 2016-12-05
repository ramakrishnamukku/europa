import React, {Component} from 'react'
import ActionBinder from './../util/ActionBinder'
// Actions
import * as AddRegistryActions from './../actions/AddRegistryActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		this.state = {
			newRegistry: {
				provider: '',
				region: '',
				key: '',
				secret: ''
			},
		};
	}
	getChildContext(){
		return {
			actions: ActionBinder([AddRegistryActions], this)
		};
	}
	componentDidMount() {
		
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
					<h2>
						Europa
					</h2>
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
    userInfo: React.PropTypes.object,
    csfrToken: React.PropTypes.string
};


import React, {Component} from 'react'
import { Link } from 'react-router'
import Btn from './../components/Btn'
import ErrorMsg from './../components/ErrorMsg'

export default class Repositories extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Monitored Repositories
					</h2>
					<div className="FlexRow">
						<div className="Flex1 Column">
							<Link to="/new-repository">
								<Btn text="Add Repository"
									 onClick={ () => {} } />
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/new-registry">
								<Btn text="Add Registry"
									 onClick={ () => {} } />
							</Link>
						</div>
					</div>
				</div>
			</div>
		);
	}
}

Repositories.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Repositories.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
import React, {Component} from 'react'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		this.state = {};
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
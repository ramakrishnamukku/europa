import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'

export default class EditRegistryPage extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillMount() {
		if(!this.context.state.addRegistry.isEdit) {
			this.context.router.push('/new-registry')
		} 
	}
	renderEditRegistry(){
		return (
			<AddRegistry 
				standaloneMode={true}
				isEdit={true}
			/>
		)
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Edit Registry
					</h2>
				</div>
				<div>
					{this.renderEditRegistry()}
				</div>
			</div>
		);
	}
}

EditRegistryPage.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

EditRegistryPage.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
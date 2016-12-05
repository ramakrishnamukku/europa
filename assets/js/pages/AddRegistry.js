import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'

export default class AddRegistry extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderSelectProvider(){
		return (
			<div className="FlexColumn">
				<label>
					Docker Registry Provider
				</label>
				<input className="BlueBorder FullWidth" 
					   placeholder="Select Amazon Container Registry or Google Container Registry" />
			</div>
		);
	}
	renderAddRegistry(){
		let row = {
			columns: [{
                icon:'icon icon-dis-docker',
                renderBody: this.renderSelectProvider
            }]
		};

		return (
			<ContentRow 
				row={row}
			/>
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Let's get started...
				</h2>
				<div>
					{this.renderAddRegistry()}
				</div>
			</div>
		);
	}
}
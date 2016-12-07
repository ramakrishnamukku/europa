import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'

export default class AddRepository extends Component {
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
				<select className="BlueBorder FullWidth" onChange={(e) => this.context.actions.updateNewRegistryField('registryProvider', e)}>
				   <option value="">Select Amazon Container Registry or Google Container Registry</option>
				   <option value="GCE">Google Container Registry</option>
				   <option value="AWS">Amazon Container Registry</option>
				   <option value="DOCKERHUB">DockerHub</option>
				   <option value="PRIVATE">Private Registry</option>
				</select>
			</div>
		);
	}
	renderDockerRepository(){
		return (
			<div className="FlexColumn">
				<label>
					Docker Image Repository
				</label>
				<input className="BlueBorder FullWidth"
						       placeholder="Enter Key Name.."
							   onChange={(e) => this.context.actions.updateNewRegistryField('registryProvider', e)} />
			</div>
		);
	}
	renderAddRepository(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-docker',
                renderBody: this.renderSelectProvider.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-container',
                renderBody: this.renderDockerRepository.bind(this)
            }]
		}];

		return rows.map(this.renderContentRow);
	}
	renderContentRow(row, index){
		return (
			<ContentRow key={index}
						row={row} />
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						New Repository
					</h2>
				</div>
				<div>
					{this.renderAddRepository()}
				</div>
			</div>
		);
	}
}

AddRepository.childContextTypes = {
    actions: React.PropTypes.object
};

AddRepository.contextTypes = {
    actions: React.PropTypes.object
};

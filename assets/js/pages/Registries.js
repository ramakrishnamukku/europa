import React, {Component} from 'react'
import ReactTooltip from 'react-tooltip'
import ContentRow from './../components/ContentRow'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import CenteredConfirm from './../components/CenteredConfirm'
import Loader from './../components/Loader'

export default class Registries extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRegistries();
	}
	renderRegistries(){
		return (
			<div className="RegistryList FlexColumn">
				{this.context.state.registries.map(this.renderRegistryItem.bind(this))}
			</div>
		);
	}
	renderRegistryItem(reg, index){
		return (
			<div key={index}
				 className="Flex1 RegistryItem FlexColumn">
				 <div className="Inside FlexRow">
				<img className="ProviderIcon"
				     src={RegistryProviderIcons(reg.provider)}/>
				<span className="Provider">
					{reg.provider}
				</span>
				<span className="Key">
				&nbsp;&ndash;&nbsp;{reg.description}
				</span>
				<span className="Region">
					<span className="Label">
						Region:&nbsp;
					</span>
					{reg.region}
				</span>
				<span className="Pipe">|</span>
				<span className="Actions">
					<i className="icon icon-dis-edit" data-tip="Edit Credentials" data-for="ToolTip"/>
					<i className="icon icon-dis-trash" data-tip="Delete Credentials" data-for="ToolTip" 
						onClick={() => this.context.actions.setRegistryForDelete(reg)}
					/>
				</span>
				</div>
				{this.renderConfirmDeleteRegistry(reg)}
			</div>
		);
	}
	renderConfirmDeleteRegistry(reg){
		if(reg == this.context.state.registry.registrySelectedForDelete) {

			if(this.context.state.registry.deleteRegistryXHR) {
				return (
					<Loader />
				);
			}
			else {
				return (
					<CenteredConfirm message="Are you sure you want to delete this registry?"
								     confirmButtonText="Delete"
								     confirmButtonStyle={{}}
								     onConfirm={() => this.context.actions.deleteRegistry()}
								     onCancel={() => this.context.actions.setRegistryForDelete(null)}/>
				);	
			}
			
		}
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Registries
					</h2>
				</div>
				<div>
					{this.renderRegistries()}
				</div>
			</div>
		);
	}
}

Registries.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Registries.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'
import NPECheck from './../util/NPECheck'
import CopyToClipboard from './../util/CopyToClipboard'
import RepoPullCommand from './../util/RepoPullCommand'
import CleanSha from './../util/CleanSha'


export default class DockerPullCommands extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		document.body.addEventListener('click', this.clickListener.bind(this));
	}
	clickListener(e){
		let ignoreClassNames = ['PullCommandsDropDown', 'LargePullCommand', 'TagPullCommand', 'icon icon-dis-copy', 'NoClick'];
		let className = e.target.className;

		if(ignoreClassNames.includes(className)){
			 return;
		}

		if(NPECheck(this.props, 'repoDetails/showPullCommands', false)) {
			this.context.actions.toggleShowPullCommands();
		}
	}
	renderMore(selectedManifests){
		if(selectedManifests && selectedManifests.length) {
			return (
				<div className="More" onClick={() => this.context.actions.toggleShowPullCommands()}>View selected tag pull commands...</div>
			);
		}
	}
	renderPullCommands(selectedManifests, activeRepo){
		if(NPECheck(this.props, 'repoDetails/showPullCommands', false)) {
			let pullCommand = RepoPullCommand(activeRepo, this.props.dnsName, this.props.ctx);

			return (
				<div className="PullCommandsDropDown">
					<div className="Arrow"></div>
					<div className="FlexColumn">
						{selectedManifests.map((manifest, index) => {
							return (
								<div className="FlexColumn" key={index}>
									<div className="LargePullCommand">
										<div style={{display: 'none'}} id={manifest.manifestId}>
											{`${pullCommand}@${manifest.manifestId}`}
										</div>
										<div className="NoClick" data-tip={manifest.manifestId} data-for="ToolTipTop">{`${pullCommand}@${CleanSha(manifest.manifestId)}..`}</div>
										<i className="icon icon-dis-copy" 
										    data-tip="Copy Pull Command" 
										    data-for="ToolTipTop"
										    onClick={() => CopyToClipboard(document.getElementById(manifest.manifestId))}/>
									</div>
									{manifest.tags.map((tag, index) => {
										return (
											<div key={index} className="SmallPullCommand">
												<i className="icon icon-dis-tag" />
												<div id={(manifest.manifestId + tag)}>
													{`${pullCommand}:${tag}`}	
												</div>
												<i className="icon icon-dis-copy" 
												   data-tip="Copy Pull Command" 
												   data-for="ToolTipTop"
												   onClick={() => CopyToClipboard(document.getElementById((manifest.manifestId + tag)))}/>
											</div>
										);
									})}
								</div>				
							);
						})}
					</div>
				</div>
			);
		}
	}
	render(){
		let activeRepo = NPECheck(this.props, 'repoDetails/activeRepo', {});
		let selectedManifests = NPECheck(this.props, 'repoDetails/selectedManifests', []);
		let pullCommand = RepoPullCommand(activeRepo, this.props.dnsName, this.props.ctx);
		
		
		return (
			<div className="DockerPullCommands">
				<div className="FlexRow Flex1">
					<i className="icon icon-dis-copy" 
					   data-for="ToolTipTop" 
					   data-tip="Copy Commands" 
					   onClick={() => CopyToClipboard(document.getElementById('copyCommands'))}/>
					<div id="copyCommands"className="Commands">{pullCommand}</div>
				</div>
				<div className="FlexRow Flex1">
					<i className="icon icon-dis-blank"/>
					{this.renderMore(selectedManifests)}
					{this.renderPullCommands(selectedManifests, activeRepo)}
				</div>
			</div>
		);
	}		
}

DockerPullCommands.contextTypes = {
	actions: PropTypes.object
};
import React, {Component} from 'react'


export default class ErrorMsg extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderClose(){
		if(this.props.close && typeof this.props.close == 'function') {
			return (
				<i className="icon icon-dis-close Red" onClick={() => this.props.close()}/>
			)
		}
	}
	render(){
		return (
			<div className="ErrorMsg FlexColumn" style={this.props.style}>
				<p>{(this.props.text) ? this.props.text : ''}</p>
				{this.renderClose()}
			</div>
		);
	}
}


ErrorMsg.PropTypes = {
	style: React.PropTypes.object,
	text: React.PropTypes.string,
	close: React.PropTypes.func
};


import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import NPECheck from './../util/NPECheck'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import AccessDenied from './../components/AccessDenied'
import ControlRoom from './../components/ControlRoom'
import PipelineStageItem from './../components/PipelineStageItem'
import CenteredConfirm from './../components/CenteredConfirm'
import Msg from './../components/Msg'

export default class Pipelines extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  componentDidMount() {
    this.context.actions.listPipelines();
    if(this.refs['createPipeline']) {
      this.refs['createPipeline'].focus();
    }
  }
  renderNoPipelines() {
    return (
      <div className="ContentContainer">
        <div className="NoContent">
          <h3>
            You have not created a Pipeline
          </h3>
          <Btn className="LargeBlueButton"
               onClick={ this.context.actions.toggleInitNewPipeline }
               text="Add Pipeline"
               canClick={true} />
        </div>
      </div>
    );
  }
  renderNoPipelinesFound() {
    return (
      <div className="ContentContainer">
        <div className="NoContent">
          <h3>
            No Pipelines found
          </h3>
        </div>
      </div>
    );
  }
  renderNewPipeline() {
    if (this.props.pipelinesStore.initNewPipeline) {
      return (
        <div style={ {margin: "14px 0 0"} }>
          <ControlRoom componentDidMount={ function() { (this.refs['createPipeline']) ?   this.refs['createPipeline'].focus() : null }}
                       renderBodyContent={ this.newPipelineForm.bind(this) } />
        </div>
      );
    }
  }
  inputClassName(selector) {
    let hasSelector = NPECheck(this.props.pipelinesStore, 'newPipelineTemplate/errorFields', []).includes(selector)
    let className = "BlueBorder FullWidth";
    if (hasSelector) {
      className = "BlueBorder FullWidth Error";
    }

    return className;
  }
  newPipelineForm() {
    return (
      <div>
        <div className="CR_Header">
          <span className="CR_HeaderTitle">
            New Pipeline
          </span>
          <span className="CR_HeaderClose">
            <i className="icon-dis-close"
               onClick={ this.context.actions.toggleInitNewPipeline } />
          </span>
        </div>
        <div className="CR_BodyContent">
          <div className="Flex1">
            <label style={{margin: '0px'}}>
              Pipeline name
            </label>
            <input className={this.inputClassName("name")}
                   style={ {background: "#fff"} }
                   ref="createPipeline"
                   value={NPECheck(this.props.pipelinesStore, 'newPipelineTemplate/name', "")}
                   placeholder="Enter Pipeline name..."
                   onChange={(e) => this.context.actions.updateNewPipelineTemplate("name", e.target.value)} />
          </div>
          <div className="Flex1">
            {this.renderNewPipelineConfirm()}
          </div>
        </div>
      </div>
    );
  }
  renderNewPipelineConfirm() {
    if (this.props.pipelinesStore.newPipelineXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    return (
      <div>
        {this.renderNewPipelineXHRError()}
        <CenteredConfirm confirmButtonText="Create"
                         noMessage={true}
                         confirmButtonStyle={{}}
                         onConfirm={ this.context.actions.createPipeline }
                         onCancel={this.context.actions.toggleInitNewPipeline } />
      </div>
    );
  }
  renderNewPipelineXHRError() {
    let error = NPECheck(this.props, 'pipelinesStore/newPipelineXHRError', false);

    if (error) {
      return (
        <Msg text={error}
             close={() => this.context.actions.clearPipelinesXHRErrors()} />
      );
    }
  }
  renderPipelineList() {
    if (this.props.pipelinesStore.initNewPipeline) return;

    const store = this.props.pipelinesStore;
    let pipes = store.pipelines || [];

    if (store.filteredPipelines) {
      pipes = store.filteredPipelines;
      if (pipes.length == 0) {
        return this.renderNoPipelinesFound();
      }
    } else if (pipes.length == 0) {
      return this.renderNoPipelines();
    }

    return (
      <div>
        {pipes.map((pipeline, idx) => {
          return (
            <div className="PipelinesListItem"
                 key={pipeline.id + idx}>
              <span>
                <i className="icon-dis-pipeline" />
              </span>
              <span onClick={ () => this.context.router.push(`/pipelines/${pipeline.id}`) }
                    style={ { color: "#1DAFE9"} }>
                {pipeline.name}
              </span>
              <span>
                {/* Saved for delete confirm */}
              </span>
            </div>
          );
        }, this)}
      </div>
    );
  }
  renderSearchPipelines() {
    if (this.props.pipelinesStore.initNewPipeline) return;

    return (
      <input className="BlueBorder Search"
             placeholder="Search by name..."
             onChange={ (e) => this.context.actions.filterPipelines(e.target.value) } />
    );
  }
  render() {
    if (this.props.pipelinesStore.pipelinesXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    if(NPECheck(this.props, 'pipelinesStore/isBlocked', false)) {

      return (
        <AccessDenied />
      );
    }

    const buttons = [
      {
        icon: 'icon icon-dis-pipeline',
        toolTip: 'New Pipeline',
        onClick: this.context.actions.toggleInitNewPipeline,
        isActive: this.props.pipelinesStore.initNewPipeline
      }
    ]

    let pipelinesLength = NPECheck(this.props, 'pipelinesStore/pipelines/length', 0);

    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
             {`${pipelinesLength} Pipeline${(pipelinesLength != 1) ? 's' : ''}`}
          </h2>
          <div className="FlexRow">
            <div className="Flex1">
              <BtnGroup buttons={buttons} />
            </div>
          </div>
        </div>
        <div>
          {this.renderNewPipeline()}
          {this.renderSearchPipelines()}
          {this.renderPipelineList()}
        </div>
      </div>
    );
  }
}

Pipelines.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Pipelines.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

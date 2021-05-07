#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from '@aws-cdk/core';
import { InfrastructureStack } from '../lib/pipeline-stack';
import { PipelineStack } from '../lib/cicd-stack';
import { LambdaStack } from '../lib/lambda-stack';

const app = new cdk.App();
cdk.Tags.of(app).add("Project", "Aurora Demo")
const lambdaStack = new LambdaStack(app, "AuroraDemoLambda", {})
new InfrastructureStack(app, 'AuroraDemoInfrastructure', {});
new PipelineStack(app, "AuroraDemoPipeline", {
  lambdaCode: lambdaStack.lambdaCode,
  repoName: "aurora-demo"
})


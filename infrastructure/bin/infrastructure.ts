#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from '@aws-cdk/core';
import { InfrastructureStack } from '../lib/infrastructure-stack';

const app = new cdk.App();
cdk.Tags.of(app).add("Project", "Aurora Demo")
new InfrastructureStack(app, 'AuroraDemoStack', {});


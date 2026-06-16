param(
    [Parameter(Mandatory = $true)]
    [string]$AccountId,

    [Parameter(Mandatory = $true)]
    [string]$Region,

    [Parameter(Mandatory = $true)]
    [string]$RepositoryName,

    [string]$ImageTag = "latest",

    [string]$LocalImage = "aws-s3-assurance:latest"
)

$ErrorActionPreference = "Stop"

$awsCli = (Get-Command aws -ErrorAction SilentlyContinue).Source
if (-not $awsCli) {
    $defaultAwsCli = "C:\Program Files\Amazon\AWSCLIV2\aws.exe"
    if (Test-Path $defaultAwsCli) {
        $awsCli = $defaultAwsCli
    } else {
        throw "AWS CLI not found. Install AWS CLI v2 or add it to PATH."
    }
}

$repositoryUri = "$AccountId.dkr.ecr.$Region.amazonaws.com/$RepositoryName"
$taggedImage = "${repositoryUri}:$ImageTag"

Write-Host "Checking ECR repository '$RepositoryName' in region '$Region'..."
& $awsCli ecr describe-repositories --repository-names $RepositoryName --region $Region 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Repository not found. Creating it..."
    & $awsCli ecr create-repository --repository-name $RepositoryName --region $Region | Out-Null
}

Write-Host "Logging into ECR..."
$password = & $awsCli ecr get-login-password --region $Region
$password | docker login --username AWS --password-stdin "$AccountId.dkr.ecr.$Region.amazonaws.com"

Write-Host "Tagging image $LocalImage as $taggedImage..."
docker tag $LocalImage $taggedImage

Write-Host "Pushing image to ECR..."
docker push $taggedImage

Write-Host "Done: $taggedImage"
